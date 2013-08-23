package de.jungblut.writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.BitSet;

import org.apache.hadoop.io.Writable;

public class BitSetWritable implements Writable {

  private BitSet set;

  public BitSetWritable() {
    // default constructor
  }

  public BitSetWritable(BitSet set) {
    this.set = set;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    long[] longs = set.toLongArray();
    out.writeInt(longs.length);
    for (int i = 0; i < longs.length; i++) {
      out.writeLong(longs[i]);
    }
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    long[] longs = new long[in.readInt()];
    for (int i = 0; i < longs.length; i++) {
      longs[i] = in.readLong();
    }

    set = BitSet.valueOf(longs);
  }

  public BitSet getBitSet() {
    return this.set;
  }

}
