package de.jungblut.datastructure;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.junit.Test;

public class SortedFileTest extends TestCase {

  private static final String TMP_SORTED_FILES = "/tmp/sorted_files/";
  private static final String TMP_FINAL_FILE = "/tmp/sorted_files/final_file.bin";

  @Test
  public void testSortedFile() throws Exception {
    FileSystem fs = FileSystem.get(new Configuration());
    try {
      try (SortedFile<IntWritable> file = new SortedFile<>(TMP_SORTED_FILES,
          TMP_FINAL_FILE, 89, IntWritable.class)) {
        // add data descending
        for (int i = 290; i > 0; i--) {
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
        FSDataInputStream open = fs.open(path);
        int items = open.readInt();
        int last = Integer.MIN_VALUE;
        // check the order if ascending
        for (int i = 0; i < items; i++) {
          iw.readFields(open);
          assertTrue(last < iw.get());
          last = iw.get();
          readItems++;
        }
      }

      assertEquals(290, readItems);

    } finally {
      fs.delete(new Path(TMP_SORTED_FILES), true);
    }
  }
}
