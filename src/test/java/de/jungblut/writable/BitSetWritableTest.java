package de.jungblut.writable;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.BitSet;

import org.junit.Test;

public class BitSetWritableTest {

  @Test
  public void testBitSetSerde() throws Exception {

    BitSet set = new BitSet(32);
    set.set(6);
    set.set(13);
    set.set(19);
    BitSetWritable writable = new BitSetWritable(set);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    writable.write(out);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream in = new DataInputStream(bais);
    BitSetWritable read = new BitSetWritable();
    read.readFields(in);

    assertEquals(true, read.getBitSet().get(6));
    assertEquals(true, read.getBitSet().get(13));
    assertEquals(true, read.getBitSet().get(19));

    assertEquals(false, read.getBitSet().get(0));
    assertEquals(false, read.getBitSet().get(5));
    assertEquals(false, read.getBitSet().get(32));

  }

}
