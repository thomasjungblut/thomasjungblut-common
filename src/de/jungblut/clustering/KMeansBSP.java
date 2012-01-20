package de.jungblut.clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.sync.SyncException;

import de.jungblut.clustering.model.CenterMessage;
import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.DistanceMeasurer;
import de.jungblut.clustering.model.Vector;

public final class KMeansBSP extends
    BSP<Vector, NullWritable, ClusterCenter, Vector> {

  public static final Log LOG = LogFactory.getLog(KMeansBSP.class);
  private ClusterCenter[] centers;
  private int maxIterations;

  @Override
  public final void setup(
      BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
      throws IOException, InterruptedException {

    Path centroids = new Path(peer.getConfiguration().get("centroid.path"));
    FileSystem fs = FileSystem.get(peer.getConfiguration());
    SequenceFile.Reader reader = null;
    final ArrayList<ClusterCenter> centers = new ArrayList<ClusterCenter>();
    try {
      reader = new SequenceFile.Reader(fs, centroids, peer.getConfiguration());
      ClusterCenter key = new ClusterCenter();
      NullWritable value = NullWritable.get();
      while (reader.next(key, value)) {
        ClusterCenter center = new ClusterCenter(key);
        centers.add(center);
      }
    } finally {
      if (reader != null)
        reader.close();
    }

    if (centers.size() == 0) {
      throw new IllegalArgumentException(
          "Centers file must contain at least a single center!");
    } else {
      this.centers = centers.toArray(new ClusterCenter[0]);
    }

    maxIterations = peer.getConfiguration()
        .getInt("k.means.max.iterations", -1);
  }

  @Override
  public final void bsp(
      BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
      throws IOException, InterruptedException, SyncException {
    long converged = 0L;
    while (true) {
      assignCenters(peer);
      peer.sync();
      converged = updateCenters(peer);
      peer.reopenInput();
      if (converged == 0)
        break;
      if (maxIterations > 0 && maxIterations < peer.getSuperstepCount())
        break;
    }
    LOG.info("Finished! Writing the assignments...");
    recalculateAssignmentsAndWrite(peer);
  }

  private final long updateCenters(
      BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
      throws IOException {
    // this is the update step
    ClusterCenter[] msgCenters = new ClusterCenter[centers.length];
    CenterMessage msg = null;
    while ((msg = (CenterMessage) peer.getCurrentMessage()) != null) {
      ClusterCenter oldCenter = msgCenters[msg.getTag()];
      ClusterCenter newCenter = msg.getData();
      if (oldCenter == null) {
        msgCenters[msg.getTag()] = newCenter;
      } else {
        ClusterCenter average = oldCenter.average(newCenter, false);
        msgCenters[msg.getTag()] = average;
      }
    }

    long convergedCounter = 0L;
    for (int i = 0; i < msgCenters.length; i++) {
      final ClusterCenter oldCenter = centers[i];
      if (msgCenters[i] != null && oldCenter.converged(msgCenters[i])) {
        centers[i] = msgCenters[i];
        convergedCounter++;
      }
    }
    return convergedCounter;
  }

  private final void assignCenters(
      BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
      throws IOException {
    // each task has all the centers, if a center has been updated it
    // needs to be broadcasted.
    final ClusterCenter[] sumArray = new ClusterCenter[centers.length];

    // we have an assignment step
    final NullWritable value = NullWritable.get();
    final Vector key = new Vector();
    while (peer.readNext(key, value)) {
      final int lowestDistantCenter = getNearestCenter(key);
      final ClusterCenter clusterCenter = sumArray[lowestDistantCenter];
      if (clusterCenter == null) {
        sumArray[lowestDistantCenter] = new ClusterCenter(key);
      } else {
        sumArray[lowestDistantCenter].plus(key);
      }
    }
    for (int i = 0; i < sumArray.length; i++) {
      if (sumArray[i] != null) {
        // we divide our center by the internal state (how many times we added a
        // vector)
        sumArray[i].divideByInternalIncrement();
        for (String peerName : peer.getAllPeerNames()) {
          peer.send(peerName, new CenterMessage(i, sumArray[i]));
        }
      }
    }
  }

  private final int getNearestCenter(Vector key) {
    int lowestDistantCenter = 0;
    double lowestDistance = Double.MAX_VALUE;
    for (int i = 0; i < centers.length; i++) {
      double estimatedDistance = DistanceMeasurer.measureManhattanDistance(
          centers[i], key);
      // check if we have a can assign a new center, because we
      // got a lower distance
      if (estimatedDistance < lowestDistance) {
        lowestDistance = estimatedDistance;
        lowestDistantCenter = i;
      }
    }
    return lowestDistantCenter;
  }

  private final void recalculateAssignmentsAndWrite(
      BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
      throws IOException {
    final NullWritable value = NullWritable.get();
    final Vector key = new Vector();
    while (peer.readNext(key, value)) {
      final int lowestDistantCenter = getNearestCenter(key);
      peer.write(centers[lowestDistantCenter], key);
    }
  }

  public static final void main(String[] args) throws IOException,
      ClassNotFoundException, InterruptedException {

    if (args.length != 4) {
      LOG.info("USAGE: <COUNT> <K> <DIMENSION OF VECTORS> <MAXITERATIONS>");
      return;
    }
    HamaConfiguration conf = new HamaConfiguration();
    // count = 7000000 spawns arround 6 tasks for 32mb block size
    int count = Integer.parseInt(args[0]);
    int k = Integer.parseInt(args[1]);
    int dimension = Integer.parseInt(args[2]);

    conf.setInt("k.means.max.iterations", Integer.parseInt(args[3]));

    LOG.info("N: " + count + " k: " + k + " Dimension: " + dimension
        + " Iterations: " + args[3]);

    Path in = new Path("files/clustering/in/data.seq");
    Path center = new Path("files/clustering/in/center/cen.seq");
    conf.set("centroid.path", center.toString());
    Path out = new Path("files/clustering/out");

    // conf.set("fs.local.block.size", "3780214016");
    // number of cores on my machine
    conf.set("bsp.local.tasks.maximum", "12");

    BSPJob job = new BSPJob(conf, KMeansBSP.class);
    job.setJobName("KMeans Clustering");

    // job.setNumBspTask(Integer.parseInt(args[4]));

    job.setJarByClass(KMeansBSP.class);
    job.setBspClass(KMeansBSP.class);

    job.setInputPath(in);
    job.setOutputPath(out);
    job.setInputFormat(org.apache.hama.bsp.SequenceFileInputFormat.class);
    job.setOutputFormat(org.apache.hama.bsp.SequenceFileOutputFormat.class);

    job.setOutputKeyClass(ClusterCenter.class);
    job.setOutputValueClass(Vector.class);

    FileSystem fs = FileSystem.get(conf);
    // prepare the input, like deleting old versions and creating centers
    prepareInput(count, k, dimension, conf, in, center, out, fs);

    // just submit the job
    job.waitForCompletion(true);

    // reads the output
    readOutput(conf, out, fs);
  }

  private static final void readOutput(HamaConfiguration conf, Path out,
      FileSystem fs) throws IOException {
    FileStatus[] stati = fs.listStatus(out);
    for (FileStatus status : stati) {
      if (!status.isDir()) {
        Path path = status.getPath();
        LOG.debug("FOUND " + path.toString());
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        ClusterCenter key = new ClusterCenter();
        Vector v = new Vector();
        int count = 0;
        while (reader.next(key, v)) {
          LOG.info(key + " / " + v);
          if (count++ > 5) {
            break;
          }
        }
        reader.close();
      }
    }
  }

  private static final void prepareInput(int count, int k, int dimension,
      HamaConfiguration conf, Path in, Path center, Path out, FileSystem fs)
      throws IOException {
    if (fs.exists(out))
      fs.delete(out, true);

    if (fs.exists(center))
      fs.delete(out, true);

    if (fs.exists(in))
      fs.delete(in, true);

    final SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs,
        conf, center, ClusterCenter.class, NullWritable.class,
        CompressionType.NONE);
    final NullWritable value = NullWritable.get();

    final SequenceFile.Writer dataWriter = SequenceFile.createWriter(fs, conf,
        in, Vector.class, NullWritable.class, CompressionType.NONE);

    Random r = new Random();
    for (int i = 0; i < count; i++) {

      double[] arr = new double[dimension];
      for (int d = 0; d < dimension; d++) {
        arr[d] = r.nextInt(count);
      }
      Vector vector = new Vector(arr);
      dataWriter.append(vector, value);
      if (k > i) {
        centerWriter.append(new ClusterCenter(vector), value);
      } else if (k == i) {
        centerWriter.close();
      }
    }
    dataWriter.close();
  }
}
