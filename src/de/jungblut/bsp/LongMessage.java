package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class LongMessage implements Writable {

  private long tag;
  private String data;

  public LongMessage() {
  }

  public LongMessage(long tag, String data) {
    super();
    this.tag = tag;
    this.data = data;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    tag = in.readLong();
    data = in.readUTF();
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeLong(tag);
    out.writeUTF(data);
  }

  public long getTag() {
    return tag;
  }

  public String getData() {
    return data;
  }

}
