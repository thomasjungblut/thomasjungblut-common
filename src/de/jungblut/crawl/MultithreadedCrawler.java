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

import de.jungblut.crawl.extraction.ExtractionLogic;
import de.jungblut.crawl.extraction.OutlinkExtractor;

public final class MultithreadedCrawler<T extends FetchResult> {

  private static final int THREAD_POOL_SIZE = 32;
  private final ExecutorService threadPool = Executors
      .newFixedThreadPool(THREAD_POOL_SIZE);

  private final String url;
  private final ExtractionLogic<T> extractor;
  private final Thread persisterThread;
  private final FetchResultPersister<T> persister;

  private int fetches = 100000;

  public MultithreadedCrawler(String url, int fetches,
      ExtractionLogic<T> extractor, ResultWriter<T> writer) throws IOException {
    this.url = url;
    this.fetches = fetches;
    this.extractor = extractor;

    // start the persisting thread
    persister = new FetchResultPersister<>(writer);
    persisterThread = new Thread(persister);
    persisterThread.start();
  }

  public final void process() throws InterruptedException, ExecutionException {
    final Deque<String> linksToCrawl = new ArrayDeque<>();
    final HashSet<String> visited = new HashSet<>();
    final CompletionService<Set<T>> completionService = new ExecutorCompletionService<>(
        threadPool);

    long appStart = System.currentTimeMillis();

    System.out.println("Num sites to fetch " + fetches);

    int currentRunningThreads = 0;
    // seed our to crawl set with the start url
    linksToCrawl.add(url);
    while (true) {
      // batch together up to 100 items or how much in the list is
      final int length = linksToCrawl.size() > 100 ? 100 : linksToCrawl.size();
      List<String> linkList = new ArrayList<>(length);
      for (int i = 0; i < length; i++) {
        linkList.add(linksToCrawl.poll());
      }
      completionService.submit(new FetchThread<>(linkList, extractor));
      currentRunningThreads++;
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
          fetches -= set.size();
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
        Thread.sleep(1000l);
      }
      if (fetches <= 0)
        break;
    }

    persister.running = false;
    persisterThread.join();
    threadPool.shutdownNow();
    System.out.println("Took overall time of "
        + (System.currentTimeMillis() - appStart) / 1000 + "s.");
  }

  public static void main(String[] args) throws InterruptedException,
      ExecutionException, IOException {
    String seedUrl = "http://news.google.de/";
    if (args.length > 0) {
      new MultithreadedCrawler<>(seedUrl, Integer.valueOf(args[0]),
          new OutlinkExtractor(), new SequenceFileResultWriter()).process();
    } else {
      new MultithreadedCrawler<>(seedUrl, 1000, new OutlinkExtractor(),
          new SequenceFileResultWriter()).process();
    }
  }

}
