package de.jungblut.crawl;

import java.util.Set;
import java.util.concurrent.Callable;

import de.jungblut.crawl.extraction.ExtractionLogic;

public class FetchThread<T extends FetchResult> implements Callable<Set<T>> {

  private final String url;
  private final ExtractionLogic<T> extractor;

  public FetchThread(String url, ExtractionLogic<T> extractor) {
    super();
    this.url = url;
    this.extractor = extractor;
  }

  @Override
  public final Set<T> call() throws Exception {
    return extractor.extract(url);
  }

}
