package de.jungblut.crawl;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Asynchronous persister thread, taking a resultwriter and handles the logic
 * behind asynchronous writing to disk or an arbitrary sink implemented by the
 * {@link ResultWriter}. The lifecycle is the following, you can submit an
 * instance of this class as a thread and if you want to stop it you can call
 * the {@link #stop()} method. Then it will make the backed queue read-only and
 * flush all items in it to the result writer.<br/>
 * 
 * @author thomas.jungblut
 */
public final class FetchResultPersister<T extends FetchResult> implements
    Runnable {

  private static final Logger LOG = LogManager
      .getLogger(FetchResultPersister.class);

  private volatile boolean running = true;

  private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
  private final ResultWriter<T> resWriter;

  public FetchResultPersister(ResultWriter<T> resWriter) throws IOException {
    this(resWriter, new Configuration());
  }

  public FetchResultPersister(ResultWriter<T> resWriter, Configuration conf)
      throws IOException {
    this.resWriter = resWriter;
    this.resWriter.open(conf);
  }

  /**
   * Add a crawled result to the back queue. Notice that after {@link #stop()}
   * has been called, this queue will not accept any items anymore.
   */
  public final void add(final T result) {
    if (running) {
      queue.add(result);
    }
  }

  /**
   * Stop this persister and make the queue read-only.
   */
  public void stop() {
    this.running = false;
  }

  /**
   * Run logic of this {@link Runnable}. Basically if we haven't stopped, we
   * will poll the queue, if there is no item anymore we will wait for 10
   * seconds. If we have received a {@link #stop()} command, the queue will be
   * freezed and all exisiting items will be flushed to disk.
   * {@link ResultWriter} close is guaranteed to run in case of failure (guarded
   * by finally).
   */
  @Override
  public final void run() {
    long retrieved = 0L;
    while (running) {
      final T poll = queue.poll();
      if (poll != null) {
        boolean failHappend = false;
        try {
          resWriter.write(poll);
          retrieved++;
          if (retrieved % 100 == 0) {
            LOG.info("Retrieved " + retrieved + " sites!");
          }
        } catch (IOException e) {
          e.printStackTrace();
          failHappend = true;
        } finally {
          if (failHappend) {
            try {
              resWriter.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
            return;
          }
        }
      } else {
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    int toWrite = queue.size();
    try {
      while (!queue.isEmpty()) {
        final T poll = queue.poll();
        LOG.info(toWrite-- + " results to write before stopping...");
        resWriter.write(poll);
        retrieved++;
        if (retrieved % 100 == 0) {
          LOG.info("Retrieved " + retrieved + " sites!");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (resWriter != null) {
        try {
          resWriter.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    LOG.info("Retrieved " + retrieved + " sites in total!");
  }

}
