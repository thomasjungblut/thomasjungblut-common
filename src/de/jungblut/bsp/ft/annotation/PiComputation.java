package de.jungblut.bsp.ft.annotation;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPJobClient;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.ClusterStatus;
import org.apache.hama.bsp.DoubleMessage;
import org.apache.hama.bsp.FileOutputFormat;
import org.apache.hama.bsp.NullInputFormat;
import org.apache.hama.bsp.TextOutputFormat;

import de.jungblut.bsp.ft.FaultTolerantBSP;

public class PiComputation {

  private int iterations;
  private String masterTask;

  @Setup
  protected void setup(
      BSPPeer<NullWritable, NullWritable, Text, DoubleWritable> peer) {
    masterTask = peer.getPeerName(0);
    iterations = 1000;
  }

  @Superstep(1)
  protected void map(
      BSPPeer<NullWritable, NullWritable, Text, DoubleWritable> peer)
      throws IOException {
    int in = 0;
    for (int i = 0; i < iterations; i++) {
      double x = 2.0 * Math.random() - 1.0, y = 2.0 * Math.random() - 1.0;
      if ((Math.sqrt(x * x + y * y) < 1.0)) {
        in++;
      }
    }
    double data = 4.0 * (double) in / (double) iterations;
    DoubleMessage estimate = new DoubleMessage(peer.getPeerName(), data);

    peer.send(masterTask, estimate);
  }

  @Superstep(2)
  protected void aggregate(
      BSPPeer<NullWritable, NullWritable, Text, DoubleWritable> peer)
      throws IOException {
    if (peer.getPeerName().equals(masterTask)) {
      double pi = 0.0;
      int numPeers = peer.getNumCurrentMessages();
      DoubleMessage received;
      while ((received = (DoubleMessage) peer.getCurrentMessage()) != null) {
        pi += received.getData();
      }

      pi = pi / numPeers;
      peer.write(new Text("Estimated value of PI is"), new DoubleWritable(pi));
    }
  }
  
  @HaltComputation
  protected boolean halt(BSPPeer<NullWritable, NullWritable, Text, DoubleWritable> peer){
    return peer.getSuperstepCount() > 0;
  }

  public static void main(String[] args) throws ClassNotFoundException,
      IOException, InterruptedException {
    HamaConfiguration conf = new HamaConfiguration();

    conf.set("hama.superstep.class", PiComputation.class.getCanonicalName());

    BSPJob bsp = new BSPJob(conf, FaultTolerantBSP.class);
    // Set the job name
    bsp.setJobName("Pi Estimation Example");
    bsp.setBspClass(AnnotationBSP.class);
    bsp.setInputFormat(NullInputFormat.class);
    bsp.setOutputKeyClass(Text.class);
    bsp.setOutputValueClass(DoubleWritable.class);
    bsp.setOutputFormat(TextOutputFormat.class);
    Path TMP_OUTPUT = new Path("/tmp/pi-" + System.currentTimeMillis());
    FileOutputFormat.setOutputPath(bsp, TMP_OUTPUT);

    BSPJobClient jobClient = new BSPJobClient(conf);
    ClusterStatus cluster = jobClient.getClusterStatus(true);

    if (args.length > 0) {
      bsp.setNumBspTask(Integer.parseInt(args[0]));
    } else {
      bsp.setNumBspTask(cluster.getMaxTasks());
    }

    long startTime = System.currentTimeMillis();
    if (bsp.waitForCompletion(true)) {
      printOutput(conf, TMP_OUTPUT);
      System.out.println("Job Finished in "
          + (double) (System.currentTimeMillis() - startTime) / 1000.0
          + " seconds");
    }
  }

  static void printOutput(HamaConfiguration conf, Path TMP_OUTPUT)
      throws IOException {
    FileSystem fs = FileSystem.get(conf);
    FileStatus[] files = fs.listStatus(TMP_OUTPUT);
    for (int i = 0; i < files.length; i++) {
      if (files[i].getLen() > 0) {
        FSDataInputStream in = fs.open(files[i].getPath());
        IOUtils.copyBytes(in, System.out, conf, false);
        in.close();
        break;
      }
    }

    fs.delete(TMP_OUTPUT, true);
  }

}
