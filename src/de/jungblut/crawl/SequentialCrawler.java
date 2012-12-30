package de.jungblut.crawl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import de.jungblut.crawl.extraction.Extractor;

/**
 * Sequential crawler, mainly for debugging or development.
 * 
 * @author thomas.jungblut
 */
public final class SequentialCrawler<T extends FetchResult> implements
    Crawler<T> {

  private Extractor<T> extractor;
  private FetchResultPersister<T> persister;
  private Thread persisterThread;

  private int fetches = 100000;

  public SequentialCrawler(int fetches, Extractor<T> extractor,
      ResultWriter<T> writer) throws IOException {
    setup(fetches, extractor, writer);
  }

  @Override
  public final void setup(int fetches, Extractor<T> extractor,
      ResultWriter<T> writer) throws IOException {
    this.fetches = fetches;
    this.extractor = extractor;

    persister = new FetchResultPersister<>(writer);
    persisterThread = new Thread(persister);
    persisterThread.start();
  }

  @Override
  public final void process(String... seedUrl) throws InterruptedException,
      ExecutionException {
    final Deque<String> linksToCrawl = new ArrayDeque<>();
    final HashSet<String> visited = new HashSet<>();

    linksToCrawl.addAll(Arrays.asList(seedUrl));
    visited.addAll(Arrays.asList(seedUrl));

    while (fetches > 0 && !linksToCrawl.isEmpty()) {
      String urlToCrawl = linksToCrawl.poll();

      T extractedResult = extractor.extract(urlToCrawl);
      if (extractedResult != null) {
        persister.add(extractedResult);

        for (String outlink : extractedResult.outlinks) {
          if (visited.add(outlink))
            linksToCrawl.add(outlink);
        }

        fetches--;
      }
    }
    // stop the persister thread and join
    persister.stop();
    persisterThread.join();
  }

}
