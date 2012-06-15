package de.jungblut.crawl;

import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.jungblut.crawl.extraction.ExtractionLogic;
import de.jungblut.crawl.extraction.OutlinkExtractor;

public class SimpleCrawler<T extends FetchResult> {

  private static int MAX_FETCHES = 100000;

  private static boolean running = true;

  private static final int THREAD_POOL_SIZE = 32;
  private final ExecutorService threadPool = Executors
      .newFixedThreadPool(THREAD_POOL_SIZE);

  private final String url;
  private final int fetches;
  private final ExtractionLogic<T> extractor;
  private final Thread persisterThread;
  private final FetchResultPersister<T> persister;

  public SimpleCrawler(String url, int fetches, ExtractionLogic<T> extractor,
      ResultWriter<T> writer) throws IOException {
    this.url = url;
    this.fetches = fetches;
    this.extractor = extractor;

    // start the persisting thread
    persister = new FetchResultPersister<T>(writer);
    persisterThread = new Thread(persister);
    persisterThread.start();
  }

  public void process() throws IOException, InterruptedException,
      ExecutionException {
    final Deque<String> linksToCrawl = new LinkedList<>();
    final HashSet<String> visited = new HashSet<>();
    final CompletionService<Set<T>> completionService = new ExecutorCompletionService<>(
        threadPool);

    long start = System.currentTimeMillis();
    long appStart = start;

    MAX_FETCHES = fetches;
    System.out.println("Set fetches to " + MAX_FETCHES);

    long count = 0L;
    int currentRunningThreads = 0;
    // seed our to crawl set with the start url
    linksToCrawl.offer(url);
    while (true) {
      final String urlToCrawl = linksToCrawl.poll();
      if (urlToCrawl != null) {
        // TODO a single fetcher should take multiple URLs in a batch
        completionService.submit(new FetchThread<T>(urlToCrawl, extractor));
        currentRunningThreads++;
        Future<Set<T>> poll;
        if (linksToCrawl.isEmpty() || currentRunningThreads > THREAD_POOL_SIZE) {
          poll = completionService.take();
        } else {
          poll = completionService.poll();
        }
        if (poll != null) {
          Set<T> set = poll.get();
          if (set != null) {
            count++;
            if (running) {
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
          }
        }
      } else {
        Thread.sleep(500);
      }

      if (count % 100 == 0) {
        long deltaSeconds = (System.currentTimeMillis() - start) / 1000;
        System.out.println(count + " sites crawled. Took " + deltaSeconds
            + "s for a 100 element chunk!");
        System.out.println("TP of : " + (100.0f / deltaSeconds)
            + " sites per second!");
        start = System.currentTimeMillis();
        if (!running) {
          System.out.println(linksToCrawl.size() + " items left!");
        }
      }
      if (running && linksToCrawl.size() + count > MAX_FETCHES) {
        System.out.println("Crawler received STOP command!");
        running = false;
        System.out.println(linksToCrawl.size() + " sites left to crawl!");
      }
      if (count > 1 && linksToCrawl.isEmpty()) {
        break;
      }
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
      new SimpleCrawler<FetchResult>(seedUrl, Integer.valueOf(args[0]),
          new OutlinkExtractor(), new SimpleResultWriter()).process();
    } else {
      new SimpleCrawler<FetchResult>(seedUrl, MAX_FETCHES,
          new OutlinkExtractor(), new SimpleResultWriter()).process();
    }
  }

}
