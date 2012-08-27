package de.jungblut.crawl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import de.jungblut.crawl.extraction.ExtractionLogic;

public final class FetchThread<T extends FetchResult> implements
    Callable<Set<T>> {

  private final List<String> urls;
  private final ExtractionLogic<T> extractor;

  public FetchThread(List<String> url, ExtractionLogic<T> extractor) {
    super();
    this.urls = url;
    this.extractor = extractor;
  }

  @Override
  public final Set<T> call() throws Exception {
    Set<T> set = new HashSet<>();
    for (String s : urls) {
      T extract = extractor.extract(s);
      if (extract != null)
        set.add(extract);
    }
    return set;
  }

}
