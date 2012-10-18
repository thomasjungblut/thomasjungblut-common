package de.jungblut.datastructure;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * A file backed disk for adding elements and reading from them in a sequential
 * fashion.
 * 
 * @author thomas.jungblut
 */
public final class DiskList<E extends Writable> {

  // the queue can either be in write state (default when opening) or in read
  // state.
  enum State {
    READ, WRITE
  }

  private final String path;

  private State currentState = State.WRITE;

  private DataOutputStream outStream;
  private DataInputStream inStream;

  private long size;
  private long toRead;

  /**
   * Opens a new disk list at the given path. Buffered through a 8k
   * {@link BufferedOutputStream}.
   */
  public DiskList(String path) throws IOException {
    this.path = path;
    this.outStream = new DataOutputStream(new BufferedOutputStream(
        new FileOutputStream(path)));
  }

  /**
   * Writes the given element to the disk.
   */
  public void add(E element) throws IOException {
    element.write(outStream);
    size++;
  }

  /**
   * Opens for a read, closes the write implicitly.
   */
  public void openRead() throws IOException {
    closeWrite();
    currentState = State.READ;
    this.inStream = new DataInputStream(new BufferedInputStream(
        new FileInputStream(this.path)));
    this.toRead = size;
  }

  /**
   * Polls the next element from the input stream.
   * 
   * @param element the element to fill.
   * @return the same element as passed through the parameter, just filled with
   *         information. Null if there is nothing more to read.
   */
  public E poll(E element) throws IOException {
    if (toRead > 0) {
      element.readFields(inStream);
      toRead--;
      return element;
    } else {
      return null;
    }
  }

  /**
   * Closes the write.
   */
  public void closeWrite() throws IOException {
    if (outStream != null) {
      outStream.close();
      outStream = null;
    }
  }

  /**
   * Closes the read.
   */
  public void closeRead() throws IOException {
    if (inStream != null) {
      inStream.close();
      inStream = null;
    }
  }

  /**
   * Closes read and write, also deletes the file.
   */
  public void close() throws IOException {
    closeRead();
    closeWrite();
    new File(path).delete();
  }

  /**
   * @return the current state READ or WRITE.
   */
  public State getCurrentState() {
    return currentState;
  }

  /**
   * @return how many items were inserted.
   */
  public long size() {
    return size;
  }

}
