package de.jungblut.crawl;

import de.jungblut.crawl.extraction.Extractor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * {@link Callable} fetcher that extracts, for a given list of URLs and with a
 * given {@link Extractor}, the content from the list of urls. This is batched,
 * therefore a list of urls to cope with the setup cost of a thread.
 *
 * @author thomas.jungblut
 */
public final class FetchThread<T extends FetchResult> implements
        Callable<Set<T>> {

    private final List<String> urls;
    private final Extractor<T> extractor;

    public FetchThread(List<String> url, Extractor<T> extractor) {
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
