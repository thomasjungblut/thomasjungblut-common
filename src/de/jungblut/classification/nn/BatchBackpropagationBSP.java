package de.jungblut.classification.nn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.NullOutputFormat;
import org.apache.hama.bsp.sync.SyncException;

import com.google.common.base.Preconditions;

import de.jungblut.clustering.KMeansBSP;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.writable.VectorWritable;

/**
 * This is a batch model for backpropagation training for multilayer perceptrons
 * on BSP. Idea: <br/>
 * Each task is processing on a local block of the data, training a full model
 * for itself (making a forward pass and calculating the error of the output
 * neurons against the prediction). Now after you have iterated over all the
 * observations, you are going to send all the weights of your neurons and the
 * error (let's say the average error over all observations) to all the other
 * tasks. After sync, each tasks has #tasks weights for a neuron and the avg
 * prediction error, now the weights are accumulated and the backward step with
 * the error begins. When all weights are backpropagated on each task, you can
 * start reading the whole observations again and make the next epoch. (until
 * some minimum average error has been seen or maximum epochs has been reached).
 * 
 * @author thomas.jungblut
 * 
 */
public final class BatchBackpropagationBSP extends
    BSP<VectorWritable, VectorWritable, NullWritable, NullWritable> {

  /*
   * VectorWritable as key input, having the prediction as the value. Output is
   * nothing, but the network (the weights) can be exported from the master task
   * to be queried elsewhere.
   */
  private static final Log LOG = LogFactory
      .getLog(BatchBackpropagationBSP.class);
  private static final String NETWORK_LAYOUT_KEY = "ann.network.layout";
  private static final String NETWORK_OUTPUT_PATH_KEY = "ann.output.path";
  private static final String NUM_EPOCHS_KEY = "ann.num.epochs";
  private static final String MAXIMUM_GLOBAL_ERROR_KEY = "ann.max.error";
  private static final String LAMBDA_KEY = "ann.lambda";
  private static final String LEARNING_RATE_KEY = "ann.learning.rate";

  private MultilayerPerceptron network;
  private int outputLayerSize;
  private double maxError = 0.0d;
  private int maxIterations;
  private double learningRate = 0.1d;
  private double lambda = 2d;

  private int itemsRead = 0;

  @Override
  public final void setup(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer)
      throws IOException, SyncException, InterruptedException {

    Configuration configuration = peer.getConfiguration();
    String layout = configuration.get(NETWORK_LAYOUT_KEY);
    Preconditions.checkNotNull(layout);
    String[] layers = layout.split(" ");
    int[] layerArray = new int[layers.length];
    for (int i = 0; i < layers.length; i++) {
      layerArray[i] = Integer.parseInt(layers[i]);
    }
    outputLayerSize = layerArray[layers.length - 1];

    network = new MultilayerPerceptron(layerArray);
    maxIterations = configuration.getInt(NUM_EPOCHS_KEY, 1000);
    String val = configuration.get(MAXIMUM_GLOBAL_ERROR_KEY);
    if (val != null) {
      maxError = Double.parseDouble(val);
    }

    val = configuration.get(LAMBDA_KEY);
    if (val != null) {
      lambda = Double.parseDouble(val);
    }

    val = configuration.get(LEARNING_RATE_KEY);
    if (val != null) {
      learningRate = Double.parseDouble(val);
    }
  }

  @Override
  public final void bsp(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer)
      throws IOException, SyncException, InterruptedException {

    // error list can be used to track convergence of the neural network
    List<Double> errorList = new ArrayList<>();
    double currentMse = Double.MAX_VALUE;
    while (true) {
      network.resetGradients();
      DoubleVector predictionErrorSum = forwardStep(peer);
      sendErrorAndWeightsToAllPeers(peer, predictionErrorSum, itemsRead,
          network.getWeights());
      // sync to exchange messages
      peer.sync();
      DoubleVector globalAvgError = accumulateAndBackwardStep(peer);
      currentMse = globalAvgError.abs().sum();
      peer.reopenInput();
      errorList.add(currentMse);
      LOG.info(peer.getSuperstepCount() + " - " + currentMse);
      if (currentMse < maxError)
        break;
      if (maxIterations > 0 && maxIterations < peer.getSuperstepCount())
        break;
    }
    LOG.info("Finished! Overall error in the net of " + currentMse);
  }

  private DoubleVector accumulateAndBackwardStep(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer)
      throws IOException {

    DoubleVector predictionErrorSum = new DenseDoubleVector(outputLayerSize);
    // accumulated weights and derivatives
    DenseDoubleMatrix[] weights = null;
    DenseDoubleMatrix[] derivatives = null;
    int sum = 0;
    VectorWeightWritableMessage msg = null;
    while ((msg = (VectorWeightWritableMessage) peer.getCurrentMessage()) != null) {
      predictionErrorSum = predictionErrorSum.add(msg.getData());
      sum += msg.getOperations();
      if (weights == null) {
        final int length = msg.getWeights().length;
        weights = new DenseDoubleMatrix[length];
        derivatives = new DenseDoubleMatrix[length];
        for (int i = 0; i < weights.length; i++) {
          weights[i] = msg.getWeights()[i];
          derivatives[i] = msg.getDerivatives()[i];
        }
      } else {
        for (int i = 0; i < weights.length; i++) {
          weights[i] = (DenseDoubleMatrix) weights[i].add(msg.getWeights()[i]);
          derivatives[i] = (DenseDoubleMatrix) derivatives[i].add(msg
              .getDerivatives()[i]);
        }
      }
    }

    network.setAccumulatedWeights(peer.getNumPeers(), weights, derivatives);

    DoubleVector avgError = predictionErrorSum.divide(peer.getNumPeers());
    network.backwardStep(avgError);

    network.adjustWeights(sum, learningRate, lambda);
    return avgError;
  }

  private static void sendErrorAndWeightsToAllPeers(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer,
      DoubleVector predictionErrorSum, int itemsRead,
      WeightMatrix[] weightMatrices) throws IOException {
    for (String peerName : peer.getAllPeerNames()) {
      peer.send(peerName, new VectorWeightWritableMessage(predictionErrorSum,
          itemsRead, weightMatrices));
    }
  }

  private DoubleVector forwardStep(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer)
      throws IOException {
    DoubleVector outputLayerError = new DenseDoubleVector(outputLayerSize);
    final VectorWritable key = new VectorWritable();
    final VectorWritable val = new VectorWritable();
    while (peer.readNext(key, val)) {
      // we only need to count in the first superstep
      if (peer.getSuperstepCount() == 0L) {
        itemsRead++;
      }
      // forward and accumulate the error to a global summation
      outputLayerError = outputLayerError.add(network.forwardStep(
          key.getVector(), val.getVector()).abs());
    }
    return outputLayerError;
  }

  /**
   * Write the final network as binary to the configured file system, output can
   * be found under configured path of key "__ann.output.path__/model.bin".
   */
  @Override
  public final void cleanup(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer)
      throws IOException {
    // only first task writes that!
    if (peer.getPeerName().equals(peer.getPeerName(0))) {
      Path outPath = new Path(peer.getConfiguration().get(
          NETWORK_OUTPUT_PATH_KEY), "model.bin");
      FileSystem fs = FileSystem.get(peer.getConfiguration());
      FSDataOutputStream stream = null;
      try {
        stream = fs.create(outPath);
        MultilayerPerceptron.serialize(network, stream);
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (stream != null) {
          stream.close();
        }
      }
    }
  }

  public static BSPJob createJob(Configuration cnf, Path in) throws IOException {
    HamaConfiguration conf = new HamaConfiguration(cnf);
    BSPJob job = new BSPJob(conf, KMeansBSP.class);
    job.setJobName("Neural network batch training");
    job.setJarByClass(BatchBackpropagationBSP.class);
    job.setBspClass(BatchBackpropagationBSP.class);
    job.setInputPath(in);
    job.setInputFormat(org.apache.hama.bsp.SequenceFileInputFormat.class);
    job.setOutputFormat(NullOutputFormat.class);
    return job;
  }

  public static void writeInput(Configuration conf, Path in,
      List<DoubleVector> data) throws IOException {
    SequenceFile.Writer writer = null;
    try {
      writer = new SequenceFile.Writer(FileSystem.get(conf), conf, new Path(in,
          "input.seq"), VectorWritable.class, VectorWritable.class);
      for (DoubleVector v : data) {
        writer.append(new VectorWritable(v.slice(v.getLength() - 1)),
            new VectorWritable(v.slice(v.getLength() - 1, v.getLength())));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  public static void main(String[] args) throws IOException,
      ClassNotFoundException, InterruptedException {
    List<DoubleVector> dataset = sampleXOR(50000);
    Preconditions.checkArgument(dataset != null && dataset.size() > 0);
    String standardLayout = "2 3 1";

    Path in = new Path("files/neuralnet/input/");
    // trained model can be found here
    Path networkOut = new Path("files/neuralnet/output/");
    Configuration conf = new Configuration();
    conf.set(NETWORK_OUTPUT_PATH_KEY, networkOut.toString());
    conf.set(NETWORK_LAYOUT_KEY, standardLayout);
    conf.setInt(NUM_EPOCHS_KEY, 1000);
    conf.set(MAXIMUM_GLOBAL_ERROR_KEY, 0.001d + "");
    // use 2 threads for locally debugging
    conf.setInt("bsp.local.tasks.maximum", 2);

    writeInput(conf, in, dataset);

    BSPJob job = createJob(conf, in);
    if (job.waitForCompletion(true)) {

      // now read it back from the path
      FileSystem fs = FileSystem.get(conf);
      FSDataInputStream inputStream = fs
          .open(new Path(networkOut, "model.bin"));
      MultilayerPerceptron network = MultilayerPerceptron
          .deserialize(inputStream);
      // MultilayerPerceptron network = getSequentialNet(dataset);

      for (DoubleVector vec : dataset) {
        DenseDoubleVector prediction = network.predict(vec.slice(vec
            .getLength() - 1));
        LOG.info("Trained network predicted " + prediction
            + " for the real outcome of "
            + vec.slice(vec.getLength() - 1, vec.getLength()));
      }
    }
  }

  private static List<DoubleVector> sampleXOR(int i) {
    List<DoubleVector> list = new ArrayList<>();
    Random r = new Random();
    for (int k = 0; k < i; k++) {
      boolean a = r.nextBoolean();
      boolean b = r.nextBoolean();
      boolean outcome = a ^ b;
      list.add(new DenseDoubleVector(new double[] { a ? 1.0 : 0.0,
          b ? 1.0 : 0.0, outcome ? 1.0 : 0.0 }));
    }

    return list;
  }

  @SuppressWarnings("unused")
  private static MultilayerPerceptron getSequentialNet(
      List<DoubleVector> dataset) {
    MultilayerPerceptron network = new MultilayerPerceptron(new int[] { 22, 12,
        1 });
    DoubleVector[] x = new DenseDoubleVector[dataset.size()];
    DoubleVector[] y = new DenseDoubleVector[dataset.size()];
    int index = 0;
    for (DoubleVector vec : dataset) {
      x[index] = vec.slice(vec.getLength() - 1);
      y[index] = vec.slice(vec.getLength() - 1, vec.getLength());
      index++;
    }
    network.train(x, y, 1000, 0.001d, 0.4d, 2d, true);
    return network;
  }
}
