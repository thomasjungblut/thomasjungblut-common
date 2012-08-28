package de.jungblut.crawl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.jungblut.crawl.extraction.Extractor;
import de.jungblut.crawl.extraction.OutlinkExtractor;

/**
 * Fast multithreaded crawler, will start a fixed threadpool of 32 threads each
 * will be fed by 10 urls at once. Majorly designed for speed and to use all the
 * available bandwidth. Based on other internet bandwidths, you may retune the
 * parameters of threadpool sizes and how many items should be batched. For my
 * 6k ADSL it works fine by 32 threads batched on 10 urls. You may scale this
 * linearly up, since this class has almost no contention and small sequential
 * code.
 * 
 * @author thomas.jungblut
 */
public final class MultithreadedCrawler<T extends FetchResult> implements
    Crawler<T> {

  private static final int THREAD_POOL_SIZE = 32;
  private static final int BATCH_SIZE = 10;

  private final ExecutorService threadPool = Executors
      .newFixedThreadPool(THREAD_POOL_SIZE);

  private Extractor<T> extractor;
  private FetchResultPersister<T> persister;
  private Thread persisterThread;
  private int fetches = 100000;

  public MultithreadedCrawler(int fetches, Extractor<T> extractor,
      ResultWriter<T> writer) throws IOException {
    setup(fetches, extractor, writer);
  }

  @Override
  public final void setup(int fetches, Extractor<T> extractor,
      ResultWriter<T> writer) throws IOException {
    this.fetches = fetches;
    this.extractor = extractor;

    // start the persisting thread
    persister = new FetchResultPersister<>(writer);
    persisterThread = new Thread(persister);
    persisterThread.start();
  }

  /*
   * (non-Javadoc)
   * @see de.jungblut.crawl.Crawler#process(java.lang.String)
   */
  @Override
  public final void process(String seedUrl) throws InterruptedException,
      ExecutionException {
    final Deque<String> linksToCrawl = new ArrayDeque<>();
    final HashSet<String> visited = new HashSet<>();
    final CompletionService<Set<T>> completionService = new ExecutorCompletionService<>(
        threadPool);

    long appStart = System.currentTimeMillis();

    System.out.println("Num sites to fetch " + fetches);

    int currentRunningThreads = 0;
    // seed our to crawl set with the start url
    linksToCrawl.add(seedUrl);
    visited.add(seedUrl);
    // while we have not fetched enough sites yet
    while (fetches > 0) {
      // batch together up to 10 items or how much in the list is
      final int length = linksToCrawl.size() > BATCH_SIZE ? BATCH_SIZE
          : linksToCrawl.size();
      fetches -= length;
      List<String> linkList = new ArrayList<>(length);
      for (int i = 0; i < length; i++) {
        linkList.add(linksToCrawl.poll());
      }
      // submit a new thread for a batch
      completionService.submit(new FetchThread<>(linkList, extractor));
      currentRunningThreads++;
      // Now we can have a look if other threads have completed yet.
      Future<Set<T>> poll = null;
      if ((linksToCrawl.isEmpty() && currentRunningThreads > 0)
          || currentRunningThreads > THREAD_POOL_SIZE) {
        poll = completionService.take();
      } else {
        poll = completionService.poll();
      }
      if (poll != null) {
        currentRunningThreads--;
        Set<T> set = poll.get();
        if (set != null) {
          // for each of our crawling results
          for (T v : set) {
            // go through the found outlinks
            for (String out : v.outlinks) {
              // if we haven't visited them yet
              if (visited.add(out)) {
                // queue them up
                linksToCrawl.offer(out);
              }
            }
            persister.add(v);
          }
        }
      } else {
        // sleep for a second if none completed yet
        Thread.sleep(1000l);
      }
    }

    persister.stop();
    persisterThread.join();
    threadPool.shutdownNow();
    System.out.println("Took overall time of "
        + (System.currentTimeMillis() - appStart) / 1000 + "s.");
  }

  public static void main(String[] args) throws InterruptedException,
      ExecutionException, IOException {
    String seedUrl = "http://news.google.de/";
    new MultithreadedCrawler<>(1000, new OutlinkExtractor(),
        new SequenceFileResultWriter()).process(seedUrl);
  }

}
