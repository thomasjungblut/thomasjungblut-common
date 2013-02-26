package de.jungblut.datastructure;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.util.PriorityQueue;

import com.google.common.base.Preconditions;

/**
 * Sorted segment merger on disk. It maintains a heap to minimize the number of
 * comparisions made between the files.
 * 
 * @author thomas.jungblut
 * 
 * @param <M> the message type extending WritableComparable.
 */
@SuppressWarnings("rawtypes")
public final class Merger<M extends WritableComparable> {

  private final File outputFile;
  private final List<File> mergeFiles;
  private final WritableComparator comp;

  private Merger(Class<M> msgClass, File outputFile, List<File> list)
      throws IOException {
    Preconditions.checkArgument(list.size() > 0,
        "Number of merged files can not be zero or negative!");
    this.outputFile = outputFile;
    this.mergeFiles = list;
    this.comp = WritableComparator.get(msgClass);
  }

  /**
   * Merges all given files together.
   */
  private void mergeFiles() throws IOException {
    if (mergeFiles.size() == 1) {
      // just move if we have a single file
      FileSystems
          .getDefault()
          .provider()
          .move(Paths.get(mergeFiles.get(0).toURI()),
              Paths.get(outputFile.toURI()),
              StandardCopyOption.REPLACE_EXISTING);
    }

    /*
     * TODO what is faster? Merging two largest segments in a single file until
     * only one is left, or building a large file while iterating over all
     * files?
     */

    // we use a priority queue to track sorted segments and minimize the
    // comparisions between the keys.
    SegmentedPriorityQueue segments = new SegmentedPriorityQueue(
        mergeFiles.size());
    int sumItems = 0;
    for (int i = 0; i < mergeFiles.size(); i++) {
      Segment segment = new Segment(mergeFiles.get(i));
      segments.put(segment);
      sumItems += segment.getItems();
    }
    int active = mergeFiles.size();
    try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
        new FileOutputStream(outputFile)))) {
      // write the number of items in front of the merged segments
      dos.writeInt(sumItems);
      while (active > 0) {
        // merge files together
        Segment peek = segments.top();
        if (peek == null) {
          break;
        }
        dos.write(peek.getBytes(), peek.getOffset(), peek.getLength());
        if (peek.hasNext()) {
          peek.next();
        } else {
          // if we have nothing to read anymore, close it
          peek.close();
          // pop out of the prio queue
          segments.pop();
          active--;
        }
        // always adjust root of the heap
        segments.adjustTop();
      }
    }
  }

  final class SegmentedPriorityQueue extends PriorityQueue<Segment> {

    public SegmentedPriorityQueue(int items) {
      initialize(items);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean lessThan(Object a, Object b) {
      return ((Segment) a).compareTo(((Segment) b)) < 0;
    }
  }

  final class Segment implements Comparable<Segment>, Closeable {

    private final DataOutputBuffer buf = new DataOutputBuffer();
    private final DataInputStream in;
    private int items;
    private int len = -1;

    public Segment(File f) throws IOException {
      in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
      // we read how many items are expected
      items = in.readInt();
      // read the first record length
      len = WritableUtils.readVInt(in);
      // read the first record
      buf.write(in, len);
    }

    public byte[] getBytes() {
      return buf.getData();
    }

    // offset is constant zero, because we are resetting the buffer every next()
    // call.
    public int getOffset() {
      return 0;
    }

    public int getLength() {
      return len;
    }

    public int getItems() {
      return this.items;
    }

    public boolean hasNext() {
      // here it is > 1, because we don't decrement items in the constructor for
      // outside item values be read correctly and written correctly.
      return items > 1;
    }

    // sets the record one further in the file
    public void next() throws IOException {
      buf.reset();
      len = WritableUtils.readVInt(in);
      buf.write(in, len);
      items--;
    }

    @Override
    public int compareTo(Segment o) {
      return comp.compare(getBytes(), getOffset(), getLength(), o.getBytes(),
          o.getOffset(), o.getLength());
    }

    @Override
    public void close() throws IOException {
      in.close();
    }

  }

  public static <M extends WritableComparable<?>> void merge(Class<M> msgClass,
      String outputFile, String... files) throws IOException {
    merge(msgClass, outputFile, Arrays.asList(files));
  }

  public static <M extends WritableComparable<?>> void merge(Class<M> msgClass,
      String outputFile, List<String> list) throws IOException {
    List<File> files = new ArrayList<>(list.size());
    for (String s : list) {
      files.add(new File(s));
    }
    merge(msgClass, new File(outputFile), files);
  }

  public static <M extends WritableComparable<?>> void merge(Class<M> msgClass,
      File outputFile, File... files) throws IOException {
    merge(msgClass, outputFile, Arrays.asList(files));
  }

  public static <M extends WritableComparable<?>> void merge(Class<M> msgClass,
      File outputFile, List<File> list) throws IOException {
    new Merger<>(msgClass, outputFile, list).mergeFiles();
  }

}
