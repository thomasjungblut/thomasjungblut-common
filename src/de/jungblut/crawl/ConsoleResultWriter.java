package de.jungblut.crawl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple class that outputs to console.
 * 
 * @author thomas.jungblut
 * 
 */
public class ConsoleResultWriter<T extends FetchResult> extends
    ResultWriterAdapter<T> {

  private static final Log LOG = LogFactory.getLog(ConsoleResultWriter.class);

  @Override
  public void write(T result) throws IOException {
    LOG.info(result);
  }
}
