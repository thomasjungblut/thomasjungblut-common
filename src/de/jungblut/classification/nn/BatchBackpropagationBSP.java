package de.jungblut.classification.nn;

import java.io.IOException;
import java.util.List;

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

import de.jungblut.clustering.KMeansBSP;
import de.jungblut.math.DoubleVector;
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
    BSP<VectorWritable, NullWritable, NullWritable, NullWritable> {

  /*
   * VectorWritable as key input, having the prediction in the last index.
   * Output is nothing, but the network (the weights) can be exported from the
   * master task to be queried elsewhere.
   */
  private static final String NETWORK_LAYOUT_KEY = "ann.network.layout";

  @Override
  public void setup(
      BSPPeer<VectorWritable, NullWritable, NullWritable, NullWritable> peer)
      throws IOException, SyncException, InterruptedException {

    Configuration configuration = peer.getConfiguration();
    String layout = configuration.get(NETWORK_LAYOUT_KEY);
    String[] layers = layout.split(" ");
    for (int i = 0; i < layers.length; i++) {
      // input layer
      if (i == 0) {

      } else if (i == (layers.length - 1)) { // output layer

      } else {

      }
    }
  }

  @Override
  public void bsp(
      BSPPeer<VectorWritable, NullWritable, NullWritable, NullWritable> arg0)
      throws IOException, SyncException, InterruptedException {

  }

  @Override
  public void cleanup(
      BSPPeer<VectorWritable, NullWritable, NullWritable, NullWritable> peer)
      throws IOException {
    // TODO safe the network weights and stuff
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
        writer.append(new VectorWritable(v), NullWritable.get());
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

    writeInput(conf, in, dataset);

    BSPJob job = createJob(conf, in);
    job.waitForCompletion(true);

  }
}
