package de.jungblut.bsp.ft;

import java.io.IOException;

import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.sync.SyncException;

@SuppressWarnings("rawtypes")
public class FaultTolerantBSP extends BSP {

  @SuppressWarnings("unchecked")
  @Override
  public void bsp(BSPPeer peer) throws IOException, SyncException,
      InterruptedException {
    int start = peer.getConfiguration().getInt("attempt.superstep", 0);

    Class<?>[] classes = peer.getConfiguration().getClasses(
        "hama.supersteps.class", Superstep.class);

    Superstep[] supersteps = new Superstep[classes.length];
    for (int i = 0; i < classes.length; i++) {
      Superstep newInstance = (Superstep) ReflectionUtils.newInstance(
          classes[i], peer.getConfiguration());
      newInstance.setup(peer);
      supersteps[i] = newInstance;
    }

    for (int index = start; index < supersteps.length; index++) {
      Superstep superstep = supersteps[index];
      superstep.compute(peer);
      if (superstep.haltComputation(peer)) {
        break;
      }
      peer.sync();
      start = 0;
    }
  }

}
