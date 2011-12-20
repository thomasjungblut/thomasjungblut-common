package de.jungblut.clustering;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
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

  private static final double ERROR_DEACTIVATED = -1d;
  public static final Log LOG = LogFactory.getLog(KMeansBSP.class);
  private final HashMap<Integer, ClusterCenter> centers = new HashMap<Integer, ClusterCenter>();
  private int maxIterations;
  private double error = ERROR_DEACTIVATED;

  @Override
  public final void setup(
      BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
      throws IOException, InterruptedException {

    Path centroids = new Path(peer.getConfiguration().get("centroid.path"));
    FileSystem fs = FileSystem.get(peer.getConfiguration());
    SequenceFile.Reader reader = null;
    try {
      reader = new SequenceFile.Reader(fs, centroids, peer.getConfiguration());
      ClusterCenter key = new ClusterCenter();
      NullWritable value = NullWritable.get();
      int centerId = 0;
      while (reader.next(key, value)) {
        ClusterCenter center = new ClusterCenter(key);
        centers.put(centerId++, center);
      }
    } finally {
      if (reader != null)
        reader.close();
    }

    if (centers.size() == 0) {
      throw new IllegalArgumentException(
          "Centers file must contain at least a single center!");
    }

    maxIterations = peer.getConfiguration()
        .getInt("k.means.max.iterations", -1);
    String errorString = peer.getConfiguration().get("k.means.error");
    if (errorString != null) {
      error = Double.parseDouble(errorString);
    }
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
    HashMap<Integer, ClusterCenter> msgCenters = new HashMap<Integer, ClusterCenter>();
    CenterMessage msg = null;
    while ((msg = (CenterMessage) peer.getCurrentMessage()) != null) {
      ClusterCenter oldCenter = msgCenters.get(msg.getTag());
      ClusterCenter newCenter = msg.getData();
      if (oldCenter == null) {
        msgCenters.put(msg.getTag(), newCenter);
      } else {
        ClusterCenter average = oldCenter.average(newCenter, false);
        msgCenters.put(msg.getTag(), average);
      }
    }

    long convergedCounter = 0L;
    for (Entry<Integer, ClusterCenter> center : msgCenters.entrySet()) {
      ClusterCenter oldCenter = centers.get(center.getKey());

      if (error == ERROR_DEACTIVATED) {
        if (oldCenter.converged(center.getValue())) {
          centers.remove(center.getKey());
          centers.put(center.getKey(), center.getValue());
          convergedCounter++;
        }
      } else {
        if (oldCenter.converged(center.getValue(), error)) {
          centers.remove(center.getKey());
          centers.put(center.getKey(), center.getValue());
          convergedCounter++;
        }
      }
    }
    return convergedCounter;
  }

  private final void assignCenters(
      BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
      throws IOException {
    // each task has all the centers, if a center has been updated it
    // needs to be broadcasted.
    // contains the for this input fitting new calculated cluster center
    final HashMap<Integer, ClusterCenter> meanMap = new HashMap<Integer, ClusterCenter>();

    // we have an assignment step
    NullWritable value = NullWritable.get();
    Vector key = new Vector();
    while (peer.readNext(key, value)) {
      int lowestDistantCenter = getNearestCenter(key);

      final ClusterCenter clusterCenter = meanMap.get(lowestDistantCenter);
      if (clusterCenter == null) {
        meanMap.put(lowestDistantCenter, new ClusterCenter(key));
      } else {
        // if we already have a cluster center, average it with the
        // assigned vector
        final ClusterCenter average = clusterCenter.average(key);
        meanMap.put(lowestDistantCenter, average);
      }
    }
    for (Entry<Integer, ClusterCenter> entry : meanMap.entrySet()) {
      for (String peerName : peer.getAllPeerNames()) {
        peer.send(peerName, new CenterMessage(entry.getKey(), entry.getValue()));
      }
    }
  }

  private final int getNearestCenter(Vector key) {
    Integer lowestDistantCenter = null;
    double lowestDistance = Double.MAX_VALUE;
    for (Entry<Integer, ClusterCenter> center : centers.entrySet()) {
      double estimatedDistance = DistanceMeasurer.measureManhattanDistance(
          center.getValue(), key);
      // check if we have a can assign a new center, because we
      // got a lower distance
      if (lowestDistantCenter == null) {
        lowestDistantCenter = center.getKey();
        lowestDistance = estimatedDistance;
      } else {
        if (estimatedDistance < lowestDistance) {
          lowestDistance = estimatedDistance;
          lowestDistantCenter = center.getKey();
        }
      }
    }
    return lowestDistantCenter;
  }

  private final void recalculateAssignmentsAndWrite(
      BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
      throws IOException {
    NullWritable value = NullWritable.get();
    Vector key = new Vector();
    while (peer.readNext(key, value)) {
      Integer lowestDistantCenter = getNearestCenter(key);
      peer.write(centers.get(lowestDistantCenter), key);
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

    // conf.set("k.means.error", "0.5");

    Path in = new Path("files/clustering/in/data.seq");
    Path center = new Path("files/clustering/in/center/cen.seq");
    conf.set("centroid.path", center.toString());
    Path out = new Path("files/clustering/out");

    BSPJob job = new BSPJob(conf, KMeansBSP.class);
    job.setJobName("KMeans Clustering");

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
