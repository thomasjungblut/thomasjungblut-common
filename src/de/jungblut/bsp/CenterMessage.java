package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hama.bsp.BSPMessage;

import de.jungblut.clustering.model.ClusterCenter;

public final class CenterMessage extends BSPMessage {

  private int oldCenterId;
  private ClusterCenter newCenter;

  public CenterMessage() {
  }

  public CenterMessage(int key, ClusterCenter value) {
    this.oldCenterId = key;
    this.newCenter = value;
  }

  @Override
  public final void readFields(DataInput in) throws IOException {
    oldCenterId = in.readInt();
    newCenter = new ClusterCenter();
    newCenter.readFields(in);
  }

  @Override
  public final void write(DataOutput out) throws IOException {
    out.writeInt(oldCenterId);
    newCenter.write(out);
  }

  @Override
  public final Integer getTag() {
    return oldCenterId;
  }

  @Override
  public final ClusterCenter getData() {
    return newCenter;
  }

}
