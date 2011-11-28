package de.jungblut.clustering;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.io.NullWritable;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPPeer;
import org.apache.zookeeper.KeeperException;

import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.Vector;

public final class KMeansBSP extends
	BSP<Vector, NullWritable, ClusterCenter, Vector> {

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
	
	// we then emit the lowest distance to each other task? <- not too optimal
	// if a center needs to be updated, it needs to be broadcasted once again.
	// TODO this isn't too optimal, but I'm still in development.
    }

    @Override
    public final void bsp(
	    BSPPeer<Vector, NullWritable, ClusterCenter, Vector> peer)
	    throws IOException, KeeperException, InterruptedException {

	// we have an assignment step

	// and an update step

    }

    public static void main(String[] args) {

    }

}
