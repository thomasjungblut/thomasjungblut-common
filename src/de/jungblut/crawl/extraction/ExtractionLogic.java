package de.jungblut.crawl.extraction;

import de.jungblut.crawl.FetchResult;

/**
 * Simple extraction logic interface for a site and a result.
 * 
 * @author thomas.jungblut
 */
public interface ExtractionLogic<T extends FetchResult> {

  public T extract(String site);

}
