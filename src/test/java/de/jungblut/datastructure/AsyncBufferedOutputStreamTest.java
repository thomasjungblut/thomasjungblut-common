package de.jungblut.datastructure;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Test;

public class AsyncBufferedOutputStreamTest {

  @Test
  public void testHugeWrites() throws Exception {
    Path tmpDir = Files.createTempDirectory("tmp");
    File tempFile = File.createTempFile("async_test_huge", "tmp",
        tmpDir.toFile());
    tempFile.deleteOnExit();
    byte[] ones = new byte[513 * 1024];
    Arrays.fill(ones, (byte) 1);

    try (AsyncBufferedOutputStream out = new AsyncBufferedOutputStream(
        new FileOutputStream(tempFile), 512 * 1024)) {
      // write a few small data to prefill the buffer
      out.write(25);
      out.write(22);

      out.write(ones, 0, ones.length);

    }

    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(
        tempFile))) {
      int numItems = 513 * 1024;
      assertEquals(25, in.read());
      assertEquals(22, in.read());
      for (int i = 0; i < numItems; i++) {
        assertEquals(1, in.read());
      }
    }

  }

  @Test
  public void testOverflowWrites() throws Exception {
    File tempFile = File.createTempFile("async_test_overflow", "tmp", new File(
        "/tmp/"));
    tempFile.deleteOnExit();
    byte[] ones = new byte[32];
    Arrays.fill(ones, (byte) 1);
    byte[] zeros = new byte[32];
    int numItems = 200;
    // only 20 bytes buffered
    try (AsyncBufferedOutputStream out = new AsyncBufferedOutputStream(
        new FileOutputStream(tempFile), 20)) {

      // test write 32 bytes 1s, and 32 bytes 0s
      for (int i = 0; i < numItems; i++) {
        out.write(ones);
        out.write(zeros);
      }
    }

    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(
        tempFile))) {
      byte[] buf = new byte[32];
      for (int i = 0; i < numItems; i++) {
        int read = in.read(buf);
        assertEquals(32, read);
        for (int x = 0; x < 32; x++) {
          assertEquals(1, buf[x]);
        }
        read = in.read(buf);
        assertEquals(32, read);
        for (int x = 0; x < 32; x++) {
          assertEquals(0, buf[x]);
        }
      }
    }

  }

  @Test
  public void testWrites() throws Exception {
    File tempFile = File.createTempFile("async_test", "tmp", new File("/tmp/"));
    tempFile.deleteOnExit();
    byte[] ones = new byte[32];
    Arrays.fill(ones, (byte) 1);
    byte[] zeros = new byte[32];
    int numItems = 10000;
    try (AsyncBufferedOutputStream out = new AsyncBufferedOutputStream(
        new FileOutputStream(tempFile), 2 * 1024)) {

      // test write 32 bytes 1s, and 32 bytes 0s
      for (int i = 0; i < numItems; i++) {
        out.write(ones);
        out.write(zeros);
      }
    }

    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(
        tempFile))) {
      byte[] buf = new byte[32];
      for (int i = 0; i < numItems; i++) {
        int read = in.read(buf);
        assertEquals(32, read);
        for (int x = 0; x < 32; x++) {
          assertEquals(1, buf[x]);
        }
        read = in.read(buf);
        assertEquals(32, read);
        for (int x = 0; x < 32; x++) {
          assertEquals(0, buf[x]);
        }
      }
    }

  }

  @Test
  public void testRaceWrites() throws Exception {
    File tempFile = File.createTempFile("async_test_race_writes", "tmp",
        new File("/tmp/"));
    tempFile.deleteOnExit();
    byte[] ones = new byte[32];
    Arrays.fill(ones, (byte) 1);
    byte[] zeros = new byte[32];
    int numItems = 2500;
    AsyncBufferedOutputStream out = new AsyncBufferedOutputStream(
        new FileOutputStream(tempFile), 512 * 1024);
    // test write 32 bytes 1s, and 32 bytes 0s for 1gb
    for (int i = 0; i < numItems; i++) {
      out.write(ones);
      out.write(zeros);
    }
    // we wait ~2s before all items are written asynchronously
    Thread.sleep(2000L);
    // now close would wait for an item to add infinitely
    out.close();

    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(
        tempFile))) {
      byte[] buf = new byte[32];
      for (int i = 0; i < numItems; i++) {
        int read = in.read(buf);
        assertEquals(32, read);
        for (int x = 0; x < 32; x++)
          assertEquals(1, buf[x]);
        read = in.read(buf);
        assertEquals(32, read);
        for (int x = 0; x < 32; x++)
          assertEquals(0, buf[x]);
      }
    }

  }

}
