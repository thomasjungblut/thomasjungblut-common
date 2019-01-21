package de.jungblut.crawl;

import de.jungblut.crawl.extraction.Extractor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Basic Crawler Interface, all implements should implicit give a constructor
 * with the same arguments like setup and redirect the call to it.
 *
 * @param <T> the result type that can be overriden by {@link FetchResult}.
 * @author thomas.jungblut
 */
public interface Crawler<T extends FetchResult> {

    /**
     * Setups this crawler.
     *
     * @param fetches   how many maximum fetches it should do.
     * @param extractor the given {@link Extractor} to extract a
     *                  {@link FetchResult}.
     * @param writer    the {@link ResultWriter} to write the result to a sink.
     */
    public void setup(int fetches, Extractor<T> extractor, ResultWriter<T> writer)
            throws IOException;

    /**
     * Starts the crawler, starting by the seedURL. The real logic is implemented
     * by the crawler itself.
     */
    public void process(String... seedUrl) throws InterruptedException,
            ExecutionException;

}
