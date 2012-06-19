package de.jungblut.classification.nn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
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

import de.jungblut.bsp.VectorWritableMessage;
import de.jungblut.clustering.KMeansBSP;
import de.jungblut.math.DoubleVector;
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
  private static final String NUM_EPOCHS_KEY = "ann.num.epochs";
  private static final String MAXIMUM_GLOBAL_ERROR_KEY = "ann.max.error";

  private MultilayerPerceptron network;
  private int outputLayerSize;
  private double maxError = Double.MAX_VALUE;
  private int maxIterations;

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
    maxIterations = configuration.getInt(NUM_EPOCHS_KEY, 0);
    String val = configuration.get(MAXIMUM_GLOBAL_ERROR_KEY);
    if (val != null) {
      maxError = Double.parseDouble(val);
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
      DenseDoubleVector predictionErrorSum = forwardStep(peer);
      sendErrorAndWeightsToAllPeers(peer, predictionErrorSum);
      // sync to exchange messages
      peer.sync();
      // TODO do a global backward step after summing the weights and setting
      // the predicted error sum
      peer.reopenInput();
      errorList.add(currentMse);
      if (currentMse < maxError)
        break;
      if (maxIterations > 0 && maxIterations < peer.getSuperstepCount())
        break;
    }
    LOG.info("Finished! Overall error in the net of " + currentMse);
  }

  private void sendErrorAndWeightsToAllPeers(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer,
      DenseDoubleVector predictionErrorSum) throws IOException {
    // TODO send the weights of the NN
    // TODO develop an efficient way to send them
    for (String peerName : peer.getAllPeerNames()) {
      peer.send(peerName, new VectorWritableMessage(predictionErrorSum));
    }
  }

  private DenseDoubleVector forwardStep(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer)
      throws IOException {
    final DenseDoubleVector outputLayerError = new DenseDoubleVector(
        outputLayerSize);
    final VectorWritable key = new VectorWritable();
    final VectorWritable val = new VectorWritable();
    while (peer.readNext(key, val)) {
      // forward and accumulate the error to a global summation
      outputLayerError
          .add(network.forwardStep(key.getVector(), val.getVector()));
    }
    return outputLayerError;
  }

  @Override
  public final void cleanup(
      BSPPeer<VectorWritable, VectorWritable, NullWritable, NullWritable> peer)
      throws IOException {
    // TODO safe the network weights and stuff
    LOG.info(network);
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
          "input.seq"), VectorWritable.class, NullWritable.class);
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
    // dataset contains features and prediction in the last element
    List<DoubleVector> dataset = MushroomReader.readMushroomDataset();
    Preconditions.checkArgument(dataset != null && dataset.size() > 0);
    // this layout is separating the layers with a whitespace
    // so we have 22 input neurons, a hidden layer with 12 neurons and an
    // output layer with a single neuron
    String standardLayout = "22 12 1";

    Path in = new Path("files/neuralnet/input/");
    Configuration conf = new Configuration();
    conf.set(NETWORK_LAYOUT_KEY, standardLayout);
    conf.setInt(NUM_EPOCHS_KEY, 1000);
    // use 2 threads for locally debugging
    conf.setInt("bsp.local.tasks.maximum", 2);

    writeInput(conf, in, dataset);

    BSPJob job = createJob(conf, in);
    job.waitForCompletion(true);

  }
}
