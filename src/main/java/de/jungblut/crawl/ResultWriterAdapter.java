package de.jungblut.crawl;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

/**
 * Empty Adapter class for a {@link ResultWriter}.
 */
public class ResultWriterAdapter<T extends FetchResult> implements
    ResultWriter<T> {

  @Override
  public void open(Configuration conf) throws IOException {

  }

  @Override
  public void write(T result) throws IOException {

  }

  @Override
  public void close() throws Exception {

  }

}
