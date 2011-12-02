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
import de.jungblut.clustering.model.Vector;

public final class KMeansBSP extends
	BSP<Vector, NullWritable, ClusterCenter, Vector> {

    private static final Log LOG = LogFactory.getLog(KMeansBSP.class);

    private LinkedList<Vector> vectors = new LinkedList<Vector>();
    private LinkedList<ClusterCenter> centers = new LinkedList<ClusterCenter>();

    @Override
    public void setup(BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
	    throws IOException, KeeperException, InterruptedException {
	// each task has all the centers, if a center has been updated it needs
	// to be broadcasted.
	// TODO we need a reset method to read all over the files

	// the plan is easy, we have much less centers (k) than records (n). So
	// we can iterate on disk via Hamas IO over the vectors and measure the
	// distance against the centers.

	// we then emit the lowest distance to each other task? <- not too
	// optimal
	// if a center needs to be updated, it needs to be broadcasted once
	// again.
	// TODO this isn't too optimal, but I'm still in development.
	peer.reopenInput();
    }

    @Override
    public final void bsp(
	    BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
	    throws IOException, KeeperException, InterruptedException {

	// we have an assignment step

	// and an update step

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
	int iteration = 1;
	HamaConfiguration conf = new HamaConfiguration();
	conf.set("num.iteration", iteration + "");

	Path in = new Path("files/clustering/in/data.seq");
	Path center = new Path("files/clustering/in/center/cen.seq");
	conf.set("centroid.path", center.toString());
	Path out = new Path("files/clustering/out");

	BSPJob job = new BSPJob(conf, KMeansBSP.class);
	job.setJobName("KMeans Clustering");

	job.setJarByClass(KMeansBSP.class);

	job.setInputPath(in);
	FileSystem fs = FileSystem.get(conf);
	if (fs.exists(out))
	    fs.delete(out, true);

	if (fs.exists(center))
	    fs.delete(out, true);

	if (fs.exists(in))
	    fs.delete(out, true);

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

	job.setOutputPath(out);
	job.setInputFormat(org.apache.hama.bsp.SequenceFileInputFormat.class);
	job.setOutputFormat(org.apache.hama.bsp.SequenceFileOutputFormat.class);

	job.setOutputKeyClass(ClusterCenter.class);
	job.setOutputValueClass(Vector.class);

	job.waitForCompletion(true);

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

}
