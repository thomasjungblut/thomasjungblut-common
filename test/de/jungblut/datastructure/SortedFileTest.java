package de.jungblut.datastructure;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableUtils;
import org.junit.Test;

public class SortedFileTest extends TestCase {

  private static final String TMP_SORTED_FILES = "/tmp/sorted_files/";
  private static final String TMP_FINAL_FILE = "/tmp/final_file.bin";

  @Test
  public void testMergedFile() throws Exception {
    FileSystem fs = FileSystem.get(new Configuration());
    fs.mkdirs(new Path(TMP_SORTED_FILES));
    try {
      try (SortedFile<IntWritable> file = new SortedFile<>(TMP_SORTED_FILES,
          TMP_FINAL_FILE, 89, IntWritable.class)) {
        // add data descending
        for (int i = 289; i >= 0; i--) {
          file.collect(new IntWritable(i));
        }
      }

      try (DataInputStream in = new DataInputStream(new FileInputStream(
          TMP_FINAL_FILE))) {
        int numItems = in.readInt();
        assertEquals(290, numItems);
        IntWritable iw = new IntWritable();
        for (int i = 0; i < numItems; i++) {
          iw.readFields(in);
          assertEquals(i, iw.get());
        }
      }

    } finally {
      fs.delete(new Path(TMP_SORTED_FILES), true);
      fs.delete(new Path(TMP_FINAL_FILE), true);
    }
  }

  @Test
  public void testSortedFile() throws Exception {
    FileSystem fs = FileSystem.get(new Configuration());
    fs.mkdirs(new Path(TMP_SORTED_FILES));
    try {
      int[] result = new int[290];
      Arrays.fill(result, 1);
      try (SortedFile<IntWritable> file = new SortedFile<>(TMP_SORTED_FILES,
          TMP_FINAL_FILE, 89, IntWritable.class, false)) {
        // add data descending
        for (int i = 289; i >= 0; i--) {
          file.collect(new IntWritable(i));
        }
      }
      // now check the segments
      FileStatus[] status = fs.globStatus(new Path(TMP_SORTED_FILES + "*.bin"));
      assertEquals(14, status.length);

      IntWritable iw = new IntWritable();
      int readItems = 0;
      for (FileStatus f : status) {
        Path path = f.getPath();
        try (FSDataInputStream open = fs.open(path)) {
          int items = open.readInt();
          int last = Integer.MIN_VALUE;
          // check the order if ascending
          for (int i = 0; i < items; i++) {
            assertEquals(4, WritableUtils.readVInt(open));
            iw.readFields(open);
            assertTrue(last < iw.get());
            result[iw.get()] = 0;
            last = iw.get();
            readItems++;
          }
        }
      }

      assertEquals(290, readItems);
      // check if every item is now zero
      for (int i = 0; i < result.length; i++) {
        assertEquals("Item at index " + i + " was non-zero!", 0, result[i]);
      }

    } finally {
      fs.delete(new Path(TMP_SORTED_FILES), true);
      fs.delete(new Path(TMP_FINAL_FILE), true);
    }
  }
}
