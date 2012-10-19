package de.jungblut.datastructure;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.hadoop.io.IntWritable;
import org.junit.Test;

public class PrefetchCacheTest extends TestCase {

  @Test
  public void testReadWrite() throws IOException {

    long size = 5242880;
    testInternal(size, false);
    // also test something that is smaller than the cache size
    testInternal(5, false);
  }

  @Test
  public void testReadWriteIterator() throws IOException {

    long size = 5242880;
    testInternal(size, true);
    // also test something that is smaller than the cache size
    testInternal(5, true);
  }

  public void testInternal(long size, boolean iterator) throws IOException {
    DiskList<IntWritable> list = new DiskList<>("/tmp/disklist.tmp");
    PrefetchCache<IntWritable> cache = fill(size, list);

    IntWritable instance;
    int incr = 0;
    if (!iterator) {
      while ((instance = cache.poll()) != null) {
        assertEquals(incr++, instance.get());
      }
    } else {
      for (IntWritable e : cache) {
        assertEquals(incr++, e.get());
      }
    }

    assertEquals(incr, size);

    IntWritable poll = cache.poll();
    assertNull(poll);

    list.close();
  }

  public PrefetchCache<IntWritable> fill(long size, DiskList<IntWritable> list)
      throws IOException {
    IntWritable instance = new IntWritable();
    for (int i = 0; i < size; i++) {
      instance.set(i);
      list.add(instance);
    }
    assertEquals(DiskList.State.WRITE, list.getCurrentState());
    list.openRead();
    assertEquals(DiskList.State.READ, list.getCurrentState());
    assertEquals(size, list.size());
    PrefetchCache<IntWritable> cache = null;
    cache = new PrefetchCache<>(list, IntWritable.class, 1000);

    assertNotNull(cache);
    return cache;
  }
}
