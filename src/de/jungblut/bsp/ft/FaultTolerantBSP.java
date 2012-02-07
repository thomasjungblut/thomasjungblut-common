package de.jungblut.bsp.ft;

import java.io.IOException;

import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.sync.SyncException;

@SuppressWarnings("rawtypes")
public class FaultTolerantBSP extends BSP {

	@SuppressWarnings("unchecked")
	@Override
	public void bsp(BSPPeer peer) throws IOException, SyncException, InterruptedException {
		int start = peer.getConfiguration().getInt("attempt.superstep", 0);
		Superstep[] supersteps = new Superstep[] {
				// TODO add supersteps from configured classes
		};
		int index = start;
		while (true) {
			Superstep superstep = supersteps[index];
			superstep.compute(peer);
			if (superstep.haltComputation(peer))
				break;
			peer.sync();
		}
	}

}
