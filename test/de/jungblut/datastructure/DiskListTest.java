package de.jungblut.datastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.hadoop.io.IntWritable;
import org.junit.Test;

public class DiskListTest {

  @Test
  public void testReadWrite() throws IOException {
    Path tmpDir = Files.createTempDirectory("tmp");
    File tempFile = File.createTempFile("disklist", "tmp", tmpDir.toFile());
    tempFile.deleteOnExit();
    DiskList<IntWritable> list = new DiskList<>(tempFile.toString());
    long size = 5242880;
    IntWritable instance = fill(list, size);

    for (int i = 0; i < list.size(); i++) {
      assertEquals(i, list.poll(instance).get());
    }

    try {
      list.poll(null);
      fail("Here should be a new NPE thrown.");
    } catch (NullPointerException e) {
      assertNotNull(e);
    }

    list.close();

    try {
      list.iterator();
      fail("Iteration must require an instance to be passed in the constructor..");
    } catch (RuntimeException e) {
      // intended!
    }

  }

  @Test
  public void testReadWriteIterator() throws IOException {
    Path tmpDir = Files.createTempDirectory("tmp");
    File tempFile = File.createTempFile("disklist", "tmp", tmpDir.toFile());
    tempFile.deleteOnExit();
    DiskList<IntWritable> list = new DiskList<>(tempFile.toString(),
        new IntWritable());
    long size = 5242880;
    fill(list, size);

    int incr = 0;
    for (IntWritable element : list) {
      assertEquals(incr++, element.get());
    }

    assertEquals(incr, size);
    try {
      list.poll(null);
      fail("Here should be a new NPE thrown.");
    } catch (NullPointerException e) {
      assertNotNull(e);
    }
    list.close();
  }

  public IntWritable fill(DiskList<IntWritable> list, long size)
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
    return instance;
  }

}
