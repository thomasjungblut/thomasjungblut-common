package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hama.bsp.BSPMessage;

import de.jungblut.math.DoubleVector;
import de.jungblut.writable.VectorWritable;

public final class VectorWritableMessage extends BSPMessage {

  private DoubleVector vector;

  public VectorWritableMessage() {
  }

  public VectorWritableMessage(DoubleVector vector) {
    this.vector = vector;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    vector = VectorWritable.readVector(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    VectorWritable.writeVector(vector, out);
  }

  @Override
  public DoubleVector getData() {
    return vector;
  }

  @Override
  public Object getTag() {
    return null;
  }

}
