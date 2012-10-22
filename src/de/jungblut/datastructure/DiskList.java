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
import java.util.AbstractList;
import java.util.Iterator;

import org.apache.hadoop.io.Writable;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;

/**
 * A file backed disk for adding elements and reading from them in a sequential
 * fashion.
 * 
 * @author thomas.jungblut
 */
public final class DiskList<E extends Writable> extends AbstractList<E>
    implements Iterable<E>, AutoCloseable, Closeable {

  enum State {
    READ, WRITE, ITERATING
  }

  private final String path;

  private State currentState = State.WRITE;

  private DataOutputStream outStream;
  private DataInputStream inStream;

  private long size;

  private E reusableElement;

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
   * Opens a new disk list at the given path with the given buffersize. 64k
   * seems quite optimal for most normal hard disks. With 7.2k rpm disk and
   * enough cache 512k might also be optimal.
   */
  public DiskList(String path, int bufferSize) throws IOException {
    this.path = path;
    this.outStream = new DataOutputStream(new BufferedOutputStream(
        new FileOutputStream(path), bufferSize));
  }

  /**
   * Opens a new disk list at the given path. Buffered through a 8k
   * {@link BufferedOutputStream}. <br/>
   * You can add a reusable element, that will be filled. This is certainly
   * required when using an iterator.
   */
  public DiskList(String path, E reusableElement) throws IOException {
    this(path);
    this.reusableElement = reusableElement;
  }

  /**
   * Opens a new disk list at the given path with the given buffersize. 64k
   * seems quite optimal for most normal hard disks. With 7.2k rpm disk and
   * enough cache 512k might also be optimal. You can add a reusable element,
   * that will be filled. This is certainly required when using an iterator.
   */
  public DiskList(String path, int bufferSize, E reusableElement)
      throws IOException {
    this(path, bufferSize);
    this.reusableElement = reusableElement;
  }

  /**
   * Writes the given element to the disk. Throws a {@link IORuntimeException}
   * in case of IO failure.
   */
  @Override
  public boolean add(E element) {
    try {
      element.write(outStream);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
    size++;
    return true;
  }

  /**
   * Opens for a read, closes the write implicitly.
   */
  public void openRead() throws IOException {
    closeWrite();
    currentState = State.READ;
    this.inStream = new DataInputStream(new BufferedInputStream(
        new FileInputStream(this.path)));
  }

  /**
   * Polls the next element from the input stream.
   * 
   * @param element the element to fill.
   * @return the same element as passed through the parameter, just filled with
   *         information. Null if there is nothing more to read.
   */
  public E poll(E element) throws IOException {
    element.readFields(inStream);
    return element;
  }

  /**
   * Closes the write.
   */
  private void closeWrite() throws IOException {
    if (outStream != null) {
      outStream.close();
      outStream = null;
    }
  }

  /**
   * Closes the read.
   */
  private void closeRead() throws IOException {
    if (inStream != null) {
      inStream.close();
      inStream = null;
    }
  }

  /**
   * Closes read and write, also deletes the file.
   */
  @Override
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
  @Override
  public int size() {
    return (int) size;
  }

  @Override
  public Iterator<E> iterator() {
    Preconditions
        .checkNotNull(
            reusableElement,
            "You have to provide a reusable element in your constructor to make use of the iterator!");
    if (getCurrentState() == State.READ) {
      currentState = State.ITERATING;
    } else if (getCurrentState() == State.WRITE) {
      try {
        openRead();
      } catch (IOException e) {
        throw new IORuntimeException(e);
      }
      currentState = State.ITERATING;
    } else {
      throw new IllegalArgumentException("Can not iterate while in state: "
          + getCurrentState());
    }

    return new AbstractIterator<E>() {
      long toRead = size;

      @Override
      protected E computeNext() {
        try {
          if (toRead > 0) {
            toRead--;
            return poll(reusableElement);
          } else {
            return endOfData();
          }
        } catch (IOException e) {
          throw new IORuntimeException(e);
        }
      }
    };
  }

  @Override
  public E get(int index) {
    throw new UnsupportedOperationException("Random access is not implemented!");
  }

  public static class IORuntimeException extends RuntimeException {

    private static final long serialVersionUID = 5448706404076488584L;

    public IORuntimeException() {
      super();
    }

    public IORuntimeException(Throwable e) {
      super(e);
    }

  }

}
