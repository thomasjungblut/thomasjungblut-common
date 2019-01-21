package de.jungblut.crawl.extraction;

import de.jungblut.crawl.FetchResult;

/**
 * Simple extraction logic interface for a site and a result.
 *
 * @author thomas.jungblut
 */
public interface Extractor<T extends FetchResult> {

    /**
     * Extracts from a given URL all the content needed and return it. Null if
     * nothing should be returned or could be parsed.
     */
    public T extract(String site);

}
