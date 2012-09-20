package de.jungblut.crawl;

import java.io.IOException;

/**
 * Simple class that outputs to console.
 * 
 * @author thomas.jungblut
 * 
 */
public class ConsoleResultWriter extends ResultWriterAdapter<ContentFetchResult> {

  @Override
  public void write(ContentFetchResult result) throws IOException {
    System.out.println("Title: " + result.getTitle());
    System.out.println("Text: " + result.getText());
  }

}
