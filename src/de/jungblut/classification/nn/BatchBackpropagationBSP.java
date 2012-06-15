package de.jungblut.classification.nn;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.sync.SyncException;

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
   * Output is nothing, but the network can be exported to be queried elsewhere.
   */

  @Override
  public void bsp(
      BSPPeer<VectorWritable, NullWritable, NullWritable, NullWritable> arg0)
      throws IOException, SyncException, InterruptedException {

  }

  public static void main(String[] args) {

  }

}
