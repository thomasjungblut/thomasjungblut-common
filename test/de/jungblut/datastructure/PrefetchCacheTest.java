package de.jungblut.datastructure;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.hadoop.io.IntWritable;
import org.junit.Test;

public class PrefetchCacheTest extends TestCase {

  @Test
  public void testReadWrite() throws IOException {

    long size = 5242880;
    testInternal(size);
    // also test something that is smaller than the cache size
    testInternal(5);
  }

  public void testInternal(long size) throws IOException {
    DiskList<IntWritable> list = new DiskList<>("/tmp/disklist.tmp");
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
    PrefetchCache<IntWritable> cache = null;
    try {
      cache = new PrefetchCache<>(list, IntWritable.class, 1000);
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }

    assertNotNull(cache);

    int incr = 0;
    while ((instance = cache.poll()) != null) {
      assertEquals(incr++, instance.get());
    }

    assertEquals(incr, size);

    IntWritable poll = cache.poll();
    assertNull(poll);

    list.close();
  }
}
