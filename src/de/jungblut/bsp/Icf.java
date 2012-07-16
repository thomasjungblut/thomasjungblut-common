package de.jungblut.bsp;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.NullInputFormat;
import org.apache.hama.bsp.NullOutputFormat;
import org.apache.hama.bsp.Partitioner;
import org.apache.hama.bsp.message.type.IntegerDoubleMessage;
import org.apache.hama.bsp.message.type.IntegerMessage;
import org.apache.hama.bsp.sync.SyncException;

import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.writable.VectorWritable;

public class Icf extends
    BSP<IntWritable, VectorWritable, IntWritable, Text, Writable> {

  private boolean isMaster;
  private int masterProcId;
  private int pnum;
  private int procId;
  private int localRows;
  private double[][] icf; // output matrix
  private boolean[] pivotSelected;
  private double[] diag1;
  private double[] diag2;
  private int columns;
  private int rows;
  private int p;

  private double[][] test_input = { { 9, 2, 6, 1 }, { 4, 8, 2, 4 },
      { 6, 2, 6, 6 }, { 7, 2, 6, 7 } };

  private double[][] input_matrix;

  @Override
  public void setup(
      BSPPeer<IntWritable, VectorWritable, IntWritable, Text, Writable> bspPeer) {

    if (bspPeer.getPeerName().equals(bspPeer.getPeerName(0))) {
      isMaster = true;
    }

    masterProcId = 0;

    pnum = bspPeer.getNumPeers();

    procId = Arrays.binarySearch(bspPeer.getAllPeerNames(),
        bspPeer.getPeerName());

    this.rows = 4;
    this.columns = 4;
    this.p = 3;
    localRows = computeNumLocal(rows);
    input_matrix = new double[localRows][columns];

    for (int i = 0; i < rows; i++) {
      int id = i % pnum;
      if (bspPeer.getPeerName().equals(bspPeer.getPeerName(id))) {
        input_matrix[i / pnum] = test_input[i];
      }
    }

    pivotSelected = new boolean[localRows];
    diag1 = new double[localRows];
    diag2 = new double[localRows];

    // Initialize a matrix for each peer
    icf = new double[localRows][p];

    for (int i = 0; i < localRows; i++) {
      // TODO use input matrix
      diag1[i] = input_matrix[i][pnum * i + procId];
      diag2[i] = 0;
    }

    System.out.println("The" + " total no of local rows are " + localRows);
  }

  @Override
  public void bsp(
      BSPPeer<IntWritable, VectorWritable, IntWritable, Text, Writable> bspPeer)
      throws IOException, SyncException, InterruptedException {

    for (int column = 0; column < p; column++) {
      // Find local pivot
      double pivot_value = -1;
      int pivot_index = -1;

      System.out.println("localRows :" + localRows);

      for (int i = 0; i < localRows; i++) {
        double tmp = diag1[i] - diag2[i];
        // System.out.println(tmp);
        if (pivotSelected[i] == false && tmp > pivot_value) {
          pivot_index = computeLocalToGlobal(i);
          pivot_value = tmp;

        }
      }

      System.out.println("My local pivot value is " + pivot_value + " by "
          + procId);

      // Find Global Pivot
      // We will use the IntegerDoubleMessage to send the pivot values and
      // their indexes. The pivot indexes are sent us tags and the values
      // are send as data.
      if (!isMaster) {
        IntegerDoubleMessage localPivotMessage = new IntegerDoubleMessage(
            pivot_index, pivot_value);
        bspPeer.send(bspPeer.getPeerName(masterProcId), localPivotMessage);
      }

      bspPeer.sync();

      int global_max_index = -1;
      double global_max = -1;
      if (isMaster) {
        // read the messages
        IntegerDoubleMessage message = null;
        while ((message = (IntegerDoubleMessage) bspPeer.getCurrentMessage()) != null) {
          double tmp = message.getData();
          System.out.println("Received local pivot values as " + tmp);
          int tmp_index = message.getTag();

          if (tmp > global_max) {
            global_max = tmp;
            global_max_index = tmp_index;
          }
        }

        // Compare its own local pivot
        if (pivot_value > global_max) {
          global_max = pivot_value;
          global_max_index = pivot_index;
        }

        IntegerMessage globalPivotIndex = new IntegerMessage(
            bspPeer.getPeerName(), global_max_index);
        // Broadcast to global pivot index and value
        for (int i = 0; i < bspPeer.getNumPeers(); i++) {
          bspPeer.send(bspPeer.getPeerName(i), globalPivotIndex);
        }
      }
      bspPeer.sync();

      IntegerMessage message = null;
      while ((message = (IntegerMessage) bspPeer.getCurrentMessage()) != null) {
        global_max_index = message.getData();
      }

      System.out.println("Global Pivot index " + global_max_index);
      // Set isMaster False for all peers
      isMaster = false;

      // TODO update to column
      int header_row_size = column + 1;
      int localRowId = computeGlobalToLocal(global_max_index);
      masterProcId = Math.abs(global_max_index % pnum);

      System.out.println(" Master ProcId is now " + masterProcId);

      // Set the new master
      if (bspPeer.getPeerName().equals(bspPeer.getPeerName(masterProcId))) {
        isMaster = true;
      }

      double[] header_row = new double[header_row_size];
      if (localRowId != -1) {

        System.out.println("localRowId is " + localRowId);
        // TODO update to column and input matrix
        icf[localRowId][column] = Math.sqrt(pivot_value);
        for (int j = 0; j < header_row_size; j++) {
          header_row[j] = icf[localRowId][j];
          System.out.println(header_row[j]);
        }

        // Set the bool of this peer's local id
        pivotSelected[localRowId] = true;

        // Broadcast the header row to peers
        VectorWritable arrayMessage = new VectorWritable(new DenseDoubleVector(
            header_row));
        for (int k = 0; k < bspPeer.getNumPeers(); k++) {
          if (k != procId)
            bspPeer.send(bspPeer.getPeerName(k), arrayMessage);
        }

      }

      bspPeer.sync();

      System.out.println("The current no of Messages are "
          + bspPeer.getNumCurrentMessages());

      if (localRowId == -1) {
        VectorWritable vecMessage;
        while ((vecMessage = (VectorWritable) bspPeer.getCurrentMessage()) != null) {
          header_row = vecMessage.getVector().toArray();
          System.out.println("Message received " + Arrays.toString(header_row));
          for (int i = 0; i < header_row.length; i++) {
            System.out.println("Header Row " + header_row[i]);
          }
        }
      }

      for (int i = 0; i < localRows; i++) {
        if (!pivotSelected[i])
          icf[i][column] = 0;
      }

      for (int k = 0; k < column; k++) {
        for (int i = 0; i < localRows; i++) {
          if (!pivotSelected[i])
            icf[i][column] = icf[i][column] - icf[i][k] * header_row[k];
        }
      }

      for (int i = 0; i < localRows; i++) {
        if (!pivotSelected[i])
          icf[i][column] = icf[i][column] + input_matrix[i][column];
      }

      for (int i = 0; i < localRows; i++) {
        if (!pivotSelected[i])
          icf[i][column] = icf[i][column] / header_row[column];
      }

      // Update Diagnol
      for (int i = 0; i < localRows; i++) {
        diag2[i] += icf[i][column] * icf[i][column];
      }

    }

  }

  @Override
  public void cleanup(
      BSPPeer<IntWritable, VectorWritable, IntWritable, Text, Writable> bspPeer)
      throws IOException {
    if (isMaster) {
      System.out.println("master " + procId);
      for (int i = 0; i < localRows; i++) {
        for (int j = 0; j < p; j++) {
          System.out.println("icf[" + i + "][" + j + "] = " + icf[i][j]);
        }
      }
    } else {
      for (int i = 0; i < localRows; i++) {
        for (int j = 0; j < p; j++) {
          System.out.println("icf2[" + i + "][" + j + "] = " + icf[i][j]);
        }
      }
    }
  }

  private int computeNumLocal(int numGlobal) {
    return numGlobal / pnum + (numGlobal % pnum > procId ? 1 : 0);
  }

  private int computeLocalToGlobal(int localIndex) {
    return localIndex * pnum + procId;
  }

  private int computeGlobalToLocal(int globalIndex) {
    int localId = globalIndex % pnum;
    if (localId == procId)
      return globalIndex / pnum;
    else
      return -1;
  }

  public static void main(String[] args) throws Exception {
    HamaConfiguration conf = new HamaConfiguration();

    int numTasks = 2;
    conf.setInt("bsp.local.tasks.maximum", numTasks);
    conf.set("max.iterations", "5");

    BSPJob bsp = new BSPJob(conf, Icf.class);

    // Set the job name
    bsp.setJobName("Parallel ICF");
    bsp.setBspClass(Icf.class);
    bsp.setInputFormat(NullInputFormat.class);
    bsp.setOutputFormat(NullOutputFormat.class);
    bsp.setPartitioner(MatrixRowPartitioner.class);
    bsp.setNumBspTask(numTasks);
    bsp.setJarByClass(Icf.class);

    long startTime = System.currentTimeMillis();
    if (bsp.waitForCompletion(true)) {

      System.out.println("Job Finished in "
          + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    }
  }

  private static final class MatrixRowPartitioner implements
      Partitioner<IntWritable, VectorWritable> {

    @Override
    public final int getPartition(IntWritable key, VectorWritable value,
        int numTasks) {
      return key.get() % (numTasks - 1);
    }
  }

}
