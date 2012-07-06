package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;

import org.apache.hadoop.io.Writable;

public class HashSetMessage implements Writable {

  private HashSet<Long> set;

  // default constructor for hadoop
  public HashSetMessage() {
  }

  // convenient constructor for you
  public HashSetMessage(HashSet<Long> set) {
    super();
    this.set = set;
  }

  public HashSet<Long> getData() {
    return set;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    int size = in.readInt();
    // use the size as parameter to prevent expensive resizing!
    set = new HashSet<>(size);
    for (int i = 0; i < size; i++) {
      set.add(in.readLong());
    }
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(set.size());
    for (long l : set) {
      out.writeLong(l);
    }
  }

}
