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
import org.apache.zookeeper.KeeperException;

import de.jungblut.clustering.model.CenterMessage;
import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.DistanceMeasurer;
import de.jungblut.clustering.model.Vector;

public final class KMeansBSP extends
	BSP<Vector, NullWritable, ClusterCenter, Vector> {

    public static final Log LOG = LogFactory.getLog(KMeansBSP.class);

    private final HashMap<Integer, ClusterCenter> centers = new HashMap<Integer, ClusterCenter>();

    enum Centers {
	CONVERGED
    }

    @Override
    public final void setup(
	    BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
	    throws IOException, KeeperException, InterruptedException {

	Path centroids = new Path(peer.getConfiguration().get("centroid.path"));
	FileSystem fs = FileSystem.get(peer.getConfiguration());
	SequenceFile.Reader reader = null;
	try {
	    reader = new SequenceFile.Reader(fs, centroids,
		    peer.getConfiguration());
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
	// in our first step, each center has "converged"
	// this is just there to prevent skipping the main loop in BSP
	peer.getCounter(Centers.CONVERGED).increment(centers.size());
    }

    @Override
    public final void bsp(
	    BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
	    throws IOException, KeeperException, InterruptedException {
	// the plan is easy, we have much less centers (k) than records (n). So
	// we can iterate on disk via Hamas IO over the vectors and measure the
	// distance against the centers.
	long lastCounter = 0L;
	// as long as our counter do change over the supersteps, we are
	// running this
	while (peer.getCounter(Centers.CONVERGED).getCounter() != lastCounter) {
	    lastCounter = peer.getCounter(Centers.CONVERGED).getCounter();
	    assignCenters(peer);
	    peer.sync();
	    updateCenters(peer);
	    peer.reopenInput();
	    // this sync may be needed for race conditions in counters
	    // peer.sync();
	}
	LOG.info("Finished! Writing the assignments...");
	recalculateAssignmentsAndWrite(peer);
    }

    private final void updateCenters(
	    BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
	    throws IOException {
	// this is the update step
	HashMap<Integer, ClusterCenter> msgCenters = new HashMap<Integer, ClusterCenter>();
	CenterMessage msg = null;
	while ((msg = (CenterMessage) peer.getCurrentMessage()) != null) {
	    // here we get the new centers of each other peer- we have to
	    // average on the result of every other peer.
	    // And then increment the counter if we have an update
	    ClusterCenter oldCenter = msgCenters.get(msg.getTag());
	    ClusterCenter newCenter = msg.getData();
	    if (oldCenter == null) {
		msgCenters.put(msg.getTag(), newCenter);
	    } else {
		msgCenters.put(msg.getTag(),
			oldCenter.average(newCenter, false));
	    }
	}

	for (Entry<Integer, ClusterCenter> center : msgCenters.entrySet()) {
	    ClusterCenter oldCenter = centers.get(center.getKey());
	    if (oldCenter.converged(center.getValue())) {
		centers.remove(center.getKey());
		centers.put(center.getKey(), center.getValue());
		peer.incrCounter(Centers.CONVERGED, 1L);
	    }
	}
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

	    final ClusterCenter clusterCenter = meanMap
		    .get(lowestDistantCenter);
	    if (clusterCenter == null) {
		meanMap.put(lowestDistantCenter, new ClusterCenter(key));
	    } else {
		// if we already have a cluster center, average it with the
		// assigned vector
		meanMap.put(lowestDistantCenter,
			clusterCenter.average(new ClusterCenter(key), true));
	    }
	}
	for (Entry<Integer, ClusterCenter> entry : meanMap.entrySet()) {
	    for (String peerName : peer.getAllPeerNames()) {
		peer.send(peerName,
			new CenterMessage(entry.getKey(), entry.getValue()));
	    }
	}
    }

    private final int getNearestCenter(Vector key) {
	Integer lowestDistantCenter = null;
	double lowestDistance = Double.MAX_VALUE;
	for (Entry<Integer, ClusterCenter> center : centers.entrySet()) {
	    double estimatedDistance = DistanceMeasurer.measureDistance(
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

    public static void main(String[] args) throws IOException,
	    ClassNotFoundException, InterruptedException {

	// count = 70000000 spawns arround 6 tasks
	int count = 1000;
	int k = 10;

	HamaConfiguration conf = new HamaConfiguration();

	Path in = new Path("files/clustering/in/data.seq");
	Path center = new Path("files/clustering/in/center/cen.seq");
	conf.set("centroid.path", center.toString());
	Path out = new Path("files/clustering/out");

	BSPJob job = new BSPJob(conf, KMeansBSP.class);
	job.setJobName("KMeans Clustering");

	// job.setCombinerClass(CenterCombiner.class);
	job.setJarByClass(KMeansBSP.class);
	job.setBspClass(KMeansBSP.class);
	// has no effect when the input is too small
	// job.setNumBspTask(2);

	job.setInputPath(in);
	job.setOutputPath(out);
	job.setInputFormat(org.apache.hama.bsp.SequenceFileInputFormat.class);
	job.setOutputFormat(org.apache.hama.bsp.SequenceFileOutputFormat.class);

	job.setOutputKeyClass(ClusterCenter.class);
	job.setOutputValueClass(Vector.class);

	FileSystem fs = FileSystem.get(conf);
	// prepare the input, like deleting old versions and creating centers
	prepareInput(count, k, conf, in, center, out, fs);

	// just submit the job
	job.waitForCompletion(true);

	// reads the output
	readOutput(conf, out, fs);
    }

    private static void readOutput(HamaConfiguration conf, Path out,
	    FileSystem fs) throws IOException {
	FileStatus[] stati = fs.listStatus(out);
	for (FileStatus status : stati) {
	    if (!status.isDir()) {
		Path path = status.getPath();
		LOG.debug("FOUND " + path.toString());
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path,
			conf);
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

    private static void prepareInput(int count, int k, HamaConfiguration conf,
	    Path in, Path center, Path out, FileSystem fs) throws IOException {
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

	final SequenceFile.Writer dataWriter = SequenceFile.createWriter(fs,
		conf, in, Vector.class, NullWritable.class,
		CompressionType.NONE);

	Random r = new Random();
	for (int i = 0; i < count; i++) {
	    Vector vector = new Vector(Math.abs(r.nextGaussian() * i + 1), Math.abs(r.nextGaussian()
		    * count + 1));
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
