package de.jungblut.datastructure;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * BufferedOutputStream that asynchronously flushes to disk, so callers don't
 * have to wait until the flush happens. Buffers are put into a queue that is
 * written asynchronously to disk once it is really available.
 * 
 * @author thomas.jungblut
 * 
 */
public final class AsyncBufferedOutputStream extends FilterOutputStream {

  private final FlushThread flusher = new FlushThread();
  private final Thread flusherThread = new Thread(flusher);
  private final BlockingQueue<byte[]> buffers = new LinkedBlockingQueue<byte[]>();

  private final byte[] buf;

  private int count = 0;

  public AsyncBufferedOutputStream(OutputStream out) {
    this(out, 8 * 1024);
  }

  public AsyncBufferedOutputStream(OutputStream out, int bufSize) {
    super(out);
    buf = new byte[bufSize];
    flusherThread.start();
  }

  /**
   * Flush the internal buffer by copying a new byte array into the buffer
   * blocking queue
   */
  private void flushBuffer() throws IOException {
    if (count > 0) {
      final byte[] copy = new byte[count];
      System.arraycopy(buf, 0, copy, 0, copy.length);
      buffers.add(copy);
      count = 0;
    }
  }

  /**
   * Writes the specified byte to this buffered output stream.
   * 
   * @param b the byte to be written.
   * @exception IOException if an I/O error occurs.
   */
  @Override
  public void write(int b) throws IOException {
    if (count >= buf.length) {
      flushBuffer();
    }
    buf[count++] = (byte) b;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array starting at
   * offset <code>off</code> to this buffered output stream.
   * 
   * <p>
   * Ordinarily this method stores bytes from the given array into this stream's
   * buffer, flushing the buffer to the underlying output stream as needed. If
   * the requested length is at least as large as this stream's buffer, however,
   * then this method will flush the buffer and write the bytes directly to the
   * underlying output stream. Thus redundant <code>BufferedOutputStream</code>s
   * will not copy data unnecessarily.
   * 
   * @param b the data.
   * @param off the start offset in the data.
   * @param len the number of bytes to write.
   * @exception IOException if an I/O error occurs.
   */
  @Override
  public void write(byte b[], int off, int len) throws IOException {
    if (len >= buf.length) {
      /*
       * If the request length exceeds the size of the output buffer, flush the
       * output buffer and then write the data directly. In this way buffered
       * streams will cascade harmlessly.
       */
      flushBuffer();
      out.write(b, off, len);
      return;
    }
    if (len > buf.length - count) {
      flushBuffer();
    }
    System.arraycopy(b, off, buf, count, len);
    count += len;
  }

  /**
   * Flushes this buffered output stream. It will enforce that the current
   * buffer will be queue for asynchronous flushing.
   * 
   * @exception IOException if an I/O error occurs.
   * @see java.io.FilterOutputStream#out
   */
  @Override
  public void flush() throws IOException {
    flushBuffer();
  }

  @Override
  public void close() throws IOException {
    flush();
    flusher.closed = true;
    try {
      flusherThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    out.close();
  }

  class FlushThread implements Runnable {

    boolean closed = false;

    @Override
    public void run() {
      // run the real flushing action to the underlying stream
      try {
        while (!closed) {
          byte[] take = buffers.take();
          out.write(take);
        }
        // write the last buffers to the streams
        for (byte[] buf : buffers) {
          out.write(buf);
        }
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
      }
    }
  }

}
