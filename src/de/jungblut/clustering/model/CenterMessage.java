package de.jungblut.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hama.bsp.BSPMessage;

public class CenterMessage extends BSPMessage {

    private int oldCenterId;
    private ClusterCenter newCenter;

    public CenterMessage(int key, ClusterCenter value) {
	this.oldCenterId = key;
	this.newCenter = value;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
	oldCenterId = in.readInt();
	newCenter.readFields(in);
    }

    @Override
    public void write(DataOutput out) throws IOException {
	out.writeInt(oldCenterId);
	newCenter.write(out);
    }

    @Override
    public Integer getTag() {
	return oldCenterId;
    }

    @Override
    public ClusterCenter getData() {
	return newCenter;
    }

}
