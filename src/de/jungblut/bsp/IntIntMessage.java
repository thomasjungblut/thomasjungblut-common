package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class IntIntMessage implements Writable {

  private int tag;
  private int data;

  public IntIntMessage() {
  }

  public IntIntMessage(int tag, int data) {
    super();
    this.tag = tag;
    this.data = data;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    tag = in.readInt();
    data = in.readInt();
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(tag);
    out.writeInt(data);
  }

  public int getTag() {
    return tag;
  }

  public int getData() {
    return data;
  }

}
