package de.jungblut.datastructure;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableUtils;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class MergerTest {

    private static final String TMP_SORTED_FILES = "/tmp/merger/";
    private static final String OUTPUT = "/tmp/merger/merged.bin";

    static int num = 0;

    @Test
    public void testMerging() throws IOException {
        FileSystem fs = FileSystem.get(new Configuration());
        fs.mkdirs(new Path(TMP_SORTED_FILES));
        try {
            // write two ascending sorted files in our binary format
            int[] first = new int[]{1, 2, 3, 6, 7, 9, 10, 12, 15};
            File f1 = write(first);
            // note 15 is duplicated
            int[] second = new int[]{4, 5, 8, 11, 13, 14, 15};
            File f2 = write(second);

            int[] result = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                    15, 15};

            Merger.<IntWritable>merge(IntWritable.class, new File(OUTPUT), f1, f2);

            try (DataInputStream in = new DataInputStream(new FileInputStream(OUTPUT))) {
                int numItems = in.readInt();
                assertEquals(result.length, numItems);
                IntWritable iw = new IntWritable();
                for (int i = 0; i < numItems; i++) {
                    iw.readFields(in);
                    assertEquals(result[i], iw.get());
                }
            }

        } finally {
            fs.delete(new Path(TMP_SORTED_FILES), true);
        }
    }

    static File write(int[] arr) throws IOException {
        File f = new File(TMP_SORTED_FILES, (num++) + ".bin");

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
            out.writeInt(arr.length);
            for (int i = 0; i < arr.length; i++) {
                // we use 4 bytes for an int
                WritableUtils.writeVInt(out, 4);
                out.writeInt(arr[i]);
            }
        }

        return f;
    }

}
