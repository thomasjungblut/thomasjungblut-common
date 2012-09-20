package de.jungblut.crawl;

import java.io.IOException;

/**
 * Simple class that outputs to console.
 * 
 * @author thomas.jungblut
 * 
 */
public class ConsoleResultWriter<T extends FetchResult> extends
    ResultWriterAdapter<T> {

  @Override
  public void write(T result) throws IOException {
    System.out.println(result);
  }
}
