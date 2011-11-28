package de.jungblut.clustering;

import java.io.IOException;

import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPPeer;
import org.apache.zookeeper.KeeperException;

import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.Vector;

public final class KMeansBSP extends
	BSP<ClusterCenter, Vector, ClusterCenter, Vector> {

    @Override
    public void setup(BSPPeer<ClusterCenter, Vector, ClusterCenter, Vector> peer)
	    throws IOException, KeeperException, InterruptedException {
	super.setup(peer);
	
	

    }

    @Override
    public final void bsp(
	    BSPPeer<ClusterCenter, Vector, ClusterCenter, Vector> peer)
	    throws IOException, KeeperException, InterruptedException {

	// we have an assignment step

	// and an update step

    }

}
