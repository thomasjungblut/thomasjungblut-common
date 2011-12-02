package de.jungblut.clustering;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPPeer;
import org.apache.zookeeper.KeeperException;

import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.DistanceMeasurer;
import de.jungblut.clustering.model.Vector;

public final class KMeansBSP extends
	BSP<Vector, NullWritable, ClusterCenter, Vector> {

    private static final Log LOG = LogFactory.getLog(KMeansBSP.class);

    private LinkedList<ClusterCenter> centers = new LinkedList<ClusterCenter>();

    enum Centers {
	CONVERGED
    }

    @Override
    public void setup(BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
	    throws IOException, KeeperException, InterruptedException {

	Path centroids = new Path(peer.getConfiguration().get("centroid.path"));
	FileSystem fs = FileSystem.get(peer.getConfiguration());
	SequenceFile.Reader reader = null;
	try {
	    reader = new SequenceFile.Reader(fs, centroids,
		    peer.getConfiguration());
	    ClusterCenter key = new ClusterCenter();
	    IntWritable value = new IntWritable();
	    while (reader.next(key, value)) {
		centers.add(new ClusterCenter(key));
	    }
	} finally {
	    if (reader != null)
		reader.close();
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

	while (peer.getCounter(Centers.CONVERGED).getCounter() > 0) {
	    // each task has all the centers, if a center has been updated it
	    // needs to be broadcasted.

	    // we have an assignment step
	    NullWritable value = NullWritable.get();
	    Vector key = new Vector();
	    while (peer.readNext(key, value)) {
		ClusterCenter lowestDistantCenter = null;
		double lowestDistance = Double.MAX_VALUE;
		for (ClusterCenter center : centers) {
		    double estimatedDistance = DistanceMeasurer
			    .measureDistance(center, key);
		    if (lowestDistantCenter == null) {
			lowestDistantCenter = center;
		    } else {
			if (estimatedDistance < lowestDistance) {
			    lowestDistance = estimatedDistance;
			    lowestDistantCenter = center;
			}
		    }
		}
		// send to every other peer the lowest distance for a vertex.
		// TODO we then use combiner for each vector that has been
		// assigned to a center. So we just send the "new" averaged
		// center via network.
		for (String peerName : peer.getAllPeerNames()) {
//		    peer.send(peerName, msg);
		}
	    }

	    peer.sync();

	    while (peer.getCurrentMessage() != null) {
		// here we get the new centers, and then increment the counter
	    }

	    // TODO and an update step
	    peer.reopenInput();
	}
    }

    public static void main(String[] args) throws IOException,
	    ClassNotFoundException, InterruptedException {
	int iteration = 1;
	HamaConfiguration conf = new HamaConfiguration();
	conf.set("num.iteration", iteration + "");

	Path in = new Path("files/clustering/in/data.seq");
	Path center = new Path("files/clustering/in/center/cen.seq");
	conf.set("centroid.path", center.toString());
	Path out = new Path("files/clustering/out");

	BSPJob job = new BSPJob(conf, KMeansBSP.class);
	job.setJobName("KMeans Clustering");

	job.setCombinerClass(CenterCombiner.class);
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
	prepareInput(conf, in, center, out, fs);

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
		LOG.info("FOUND " + path.toString());
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path,
			conf);
		ClusterCenter key = new ClusterCenter();
		Vector v = new Vector();
		while (reader.next(key, v)) {
		    LOG.info(key + " / " + v);
		}
		reader.close();
	    }
	}
    }

    private static void prepareInput(HamaConfiguration conf, Path in,
	    Path center, Path out, FileSystem fs) throws IOException {
	if (fs.exists(out))
	    fs.delete(out, true);

	if (fs.exists(center))
	    fs.delete(out, true);

	if (fs.exists(in))
	    fs.delete(in, true);

	final SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs,
		conf, center, ClusterCenter.class, IntWritable.class);
	final IntWritable value = new IntWritable(0);
	centerWriter.append(new ClusterCenter(new Vector(1, 1)), value);
	centerWriter.append(new ClusterCenter(new Vector(5, 5)), value);
	centerWriter.close();

	final SequenceFile.Writer dataWriter = SequenceFile.createWriter(fs,
		conf, in, ClusterCenter.class, Vector.class);
	dataWriter
		.append(new ClusterCenter(new Vector(0, 0)), new Vector(1, 2));
	dataWriter.append(new ClusterCenter(new Vector(0, 0)),
		new Vector(16, 3));
	dataWriter
		.append(new ClusterCenter(new Vector(0, 0)), new Vector(3, 3));
	dataWriter
		.append(new ClusterCenter(new Vector(0, 0)), new Vector(2, 2));
	dataWriter
		.append(new ClusterCenter(new Vector(0, 0)), new Vector(2, 3));
	dataWriter.append(new ClusterCenter(new Vector(0, 0)),
		new Vector(25, 1));
	dataWriter
		.append(new ClusterCenter(new Vector(0, 0)), new Vector(7, 6));
	dataWriter
		.append(new ClusterCenter(new Vector(0, 0)), new Vector(6, 5));
	dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(-1,
		-23));
	dataWriter.close();
    }

}
