package de.jungblut.datastructure;

import gnu.trove.list.array.TIntArrayList;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.util.IndexedSortable;
import org.apache.hadoop.util.QuickSort;

/**
 * A file that serializes WritableComparables to a buffer, once it hits a
 * threshold this buffer will be sorted in memory. After the file is closed, all
 * sorted segments are merged to a single file. Afterwards the file can be read
 * using the provided {@link WritableComparable} in order defined in it.
 * 
 * @author thomasjungblut
 * 
 * @param <M> the message type extending WritableComparable.
 */
public final class SortedFile<M extends WritableComparable<?>> implements
    IndexedSortable, Closeable {

  private static final float SPILL_TRIGGER_BUFFER_FILL_PERCENTAGE = 0.9f;
  private static final QuickSort SORTER = new QuickSort();

  private final String dir;
  private final String destinationFileName;
  private final WritableComparator comp;
  private final DataOutputBuffer buf;
  private final int bufferThresholdSize;

  private int fileCount;
  private TIntArrayList offsets;
  private TIntArrayList indices;
  private int size;

  /**
   * Creates a sorted file.
   * 
   * @param dir the directory to use for swapping, will be created if not
   *          exists.
   * @param finalFileName the final file where the data should end up merged.
   * @param bufferSize the buffersize. By default, the spill starts when 90% of
   *          the buffer is reached, so you should overallocated ~10% of the
   *          data.
   * @param msgClass the class that implements the comparable, usually the
   *          message class that will be added into collect.
   * @throws IOException in case the directory couldn't be created if not
   *           exists.
   */
  public SortedFile(String dir, String finalFileName, int bufferSize,
      Class<M> msgClass) throws IOException {
    this.dir = dir;
    this.destinationFileName = finalFileName;
    Files.createDirectories(Paths.get(dir));
    this.bufferThresholdSize = (int) (bufferSize * SPILL_TRIGGER_BUFFER_FILL_PERCENTAGE);
    this.comp = WritableComparator.get(msgClass);
    this.buf = new DataOutputBuffer(bufferSize);
    this.offsets = new TIntArrayList();
    this.indices = new TIntArrayList();
  }

  /**
   * Collects a message. If the buffer threshold is exceeded it will sort the
   * buffer and spill to disk. Note that this is synchronous, so this waits
   * until it is finished.
   * 
   * @param msg the message to add.
   * @throws IOException when an IO error happens.
   */
  public void collect(M msg) throws IOException {
    offsets.add(buf.getLength());
    msg.write(buf);
    indices.add(size);
    size++;
    if (buf.getLength() > bufferThresholdSize) {
      sortAndSpill(buf.getLength());
      offsets.clear();
      indices.clear();
      buf.reset();
      size = 0;
    }
  }

  /**
   * First sort, then spill the buffer to disk.
   * 
   * @param bufferEnd the end of the buffer.
   * @throws IOException when IO error happens.
   */
  private void sortAndSpill(int bufferEnd) throws IOException {
    // add the end of the buffer to the list for convenience
    offsets.add(bufferEnd);
    SORTER.sort(this, 0, size);
    // write to file
    try (DataOutputStream os = new DataOutputStream(new FileOutputStream(
        new File(dir, fileCount + ".bin")))) {
      // write the size in front, so we can allocate appropriate sized array
      // later on
      os.writeInt(size);
      for (int index = 0; index < size; index++) {
        // now we have to translate our sorted indices back to the raw bytes
        int x = indices.get(index);
        int off = offsets.get(x);
        int follow = x + 1;
        int len = offsets.get(follow) - off;
        os.write(buf.getData(), off, len);
      }
    }
    fileCount++;
  }

  @Override
  public int compare(int left, int right) {
    // calculate the offsets for the data.
    int leftTranslated = indices.get(left);
    int leftEndTranslated = leftTranslated + 1;
    int rightTranslated = indices.get(right);
    int rightEndTranslated = rightTranslated + 1;

    int leftOffset = offsets.get(leftTranslated);
    int rightOffset = offsets.get(rightTranslated);

    int leftLen = offsets.get(leftEndTranslated) - leftOffset;
    int rightLen = offsets.get(rightEndTranslated) - rightOffset;
    return comp.compare(buf.getData(), leftOffset, leftLen, buf.getData(),
        rightOffset, rightLen);
  }

  @Override
  public void swap(int left, int right) {
    // swaps two indices in their files.
    int tmp = indices.get(left);
    indices.set(left, indices.get(right));
    indices.set(right, tmp);
  }

  @Override
  public void close() throws IOException {
    if (buf.getLength() > 0) {
      sortAndSpill(buf.getLength());
    }
    // TODO now the files must be merged together into destinationFileName
  }

}
