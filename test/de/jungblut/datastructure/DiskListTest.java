package de.jungblut.datastructure;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.hadoop.io.IntWritable;
import org.junit.Test;

public class DiskListTest extends TestCase {

  @Test
  public void testReadWrite() throws IOException {

    DiskList<IntWritable> list = new DiskList<>("/tmp/disklist.tmp");
    long size = 5242880;
    IntWritable instance = new IntWritable();
    for (int i = 0; i < size; i++) {
      instance.set(i);
      list.add(instance);
    }
    assertEquals(DiskList.State.WRITE, list.getCurrentState());
    list.closeWrite();
    list.openRead();
    assertEquals(DiskList.State.READ, list.getCurrentState());
    assertEquals(size, list.size());

    for (int i = 0; i < list.size(); i++) {
      assertEquals(i, list.poll(instance).get());
    }

    IntWritable poll = list.poll(null);
    assertNull(poll);

    list.close();
  }
}
