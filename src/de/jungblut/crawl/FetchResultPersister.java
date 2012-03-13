package de.jungblut.crawl;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

public class FetchResultPersister<T extends FetchResult> implements Runnable {

  public boolean running = true;

  private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
  private final SequenceFile.Writer writer;
  private final ResultWriter<T> resWriter;

  public FetchResultPersister(ResultWriter<T> resWriter) throws IOException {
    this.resWriter = resWriter;
    final Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    Path out = resWriter.getOutputPath();
    fs.delete(out, true);
    writer = resWriter.getWriterInstance();
  }

  public final void add(final T result) {
    queue.offer(result);
  }

  @Override
  public final void run() {
    long retrieved = 0L;
    while (running) {
      final T poll = queue.poll();
      if (poll != null) {
        try {
          resWriter.write(writer, poll);
          retrieved++;
          if (retrieved % 100 == 0) {
            System.out.println("Retrieved " + retrieved + " sites!");
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println("Persister received STOP command!");
    int toWrite = queue.size();
    try {
      while (!queue.isEmpty()) {
        final T poll = queue.poll();
        System.out.println(toWrite-- + " results to write before stopping...");
        resWriter.write(writer, poll);
        retrieved++;
        if (retrieved % 100 == 0) {
          System.out.println("Retrieved " + retrieved + " sites!");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println("Retrieved " + retrieved + " sites in total!");
  }

}
