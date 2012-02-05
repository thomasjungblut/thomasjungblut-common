package de.jungblut.bsp.ft;

import org.apache.hama.bsp.BSPPeer;

public class Condition {
    
    private final long limit;

    public Condition(long limit) {
	super();
	this.limit = limit;
    }

    public boolean isTrue(BSPPeer peer){
	return peer.getSuperstepCount() < limit;
    }

}
