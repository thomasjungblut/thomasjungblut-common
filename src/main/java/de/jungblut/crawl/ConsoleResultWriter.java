package de.jungblut.crawl;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple class that outputs to console.
 * 
 * @author thomas.jungblut
 * 
 */
public class ConsoleResultWriter<T extends FetchResult> extends
    ResultWriterAdapter<T> {

  private static final Logger LOG = LogManager
      .getLogger(ConsoleResultWriter.class);

  @Override
  public void write(T result) throws IOException {
    LOG.info(result);
  }
}
