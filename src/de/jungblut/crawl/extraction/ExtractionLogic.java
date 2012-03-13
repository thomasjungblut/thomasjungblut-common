package de.jungblut.crawl.extraction;

import java.util.Set;

import de.jungblut.crawl.FetchResult;

/**
 * Simple extraction logic interface for a site and a result.
 * 
 * @author thomas.jungblut
 * 
 * @param <T>
 */
public interface ExtractionLogic<T extends FetchResult> {

  public Set<T> extract(String site);

}
