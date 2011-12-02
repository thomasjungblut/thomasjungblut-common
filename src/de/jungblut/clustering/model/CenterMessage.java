package de.jungblut.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hama.bsp.BSPMessage;

public class CenterMessage extends BSPMessage {

    private ClusterCenter oldCenter;
    private ClusterCenter newCenter;

    public CenterMessage(ClusterCenter key, ClusterCenter value) {
	this.oldCenter = key;
	this.newCenter = value;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
	oldCenter.readFields(in);
	newCenter.readFields(in);
    }

    @Override
    public void write(DataOutput out) throws IOException {
	oldCenter.write(out);
	newCenter.write(out);
    }

    @Override
    public ClusterCenter getTag() {
	return oldCenter;
    }

    @Override
    public ClusterCenter getData() {
	return newCenter;
    }

}
