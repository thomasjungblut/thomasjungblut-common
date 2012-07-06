package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import de.jungblut.math.DoubleVector;
import de.jungblut.writable.VectorWritable;

public final class CenterMessage implements Writable {

  private int centerIndex;
  private DoubleVector newCenter;
  private int incrementCounter;

  public CenterMessage() {
  }

  public CenterMessage(int key, DoubleVector value) {
    this.centerIndex = key;
    this.newCenter = value;
  }

  public CenterMessage(int key, int increment, DoubleVector value) {
    this.centerIndex = key;
    this.incrementCounter = increment;
    this.newCenter = value;
  }

  @Override
  public final void readFields(DataInput in) throws IOException {
    centerIndex = in.readInt();
    incrementCounter = in.readInt();
    newCenter = VectorWritable.readVector(in);
  }

  @Override
  public final void write(DataOutput out) throws IOException {
    out.writeInt(centerIndex);
    out.writeInt(incrementCounter);
    VectorWritable.writeVector(newCenter, out);
  }

  public int getCenterIndex() {
    return centerIndex;
  }

  public int getIncrementCounter() {
    return incrementCounter;
  }

  public final int getTag() {
    return centerIndex;
  }

  public final DoubleVector getData() {
    return newCenter;
  }

}
