package de.jungblut.bsp.ft;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.sync.SyncException;

public class FaultTolerantBSP extends BSP<LongWritable, Text, Text, Text> {

    @Override
    public void bsp(BSPPeer<LongWritable, Text, Text, Text> peer) throws IOException, SyncException,
	    InterruptedException {
	int start = peer.getConfiguration().getInt("attempt.superstep", 0);
	Condition condition = new Condition(5);
	while(condition.isTrue(peer)){
	    
	    peer.sync();
	}
	
    }

}
