package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hama.bsp.BSPMessage;

public class IntIntMessage extends BSPMessage {

  int tag;
  int data;

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

  /*
   * Not used because of boxing primitives.
   */

  @Override
  public Integer getTag() {
    return null;
  }

  @Override
  public Integer getData() {
    return null;
  }

}
