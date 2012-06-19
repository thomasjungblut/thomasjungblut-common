package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hama.bsp.BSPMessage;

import de.jungblut.math.DoubleVector;
import de.jungblut.writable.VectorWritable;

public final class VectorWritableMessage extends BSPMessage {

  private DoubleVector vector;
  private int operations;

  public VectorWritableMessage() {
  }

  public VectorWritableMessage(DoubleVector vector) {
    this.vector = vector;
  }

  public VectorWritableMessage(DoubleVector vector, int operations) {
    this.vector = vector;
    this.operations = operations;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    operations = in.readInt();
    vector = VectorWritable.readVector(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(operations);
    VectorWritable.writeVector(vector, out);
  }

  public int getOperations() {
    return operations;
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
