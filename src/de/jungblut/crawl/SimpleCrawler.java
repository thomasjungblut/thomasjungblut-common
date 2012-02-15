package de.jungblut.crawl;

import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.*;

public class SimpleCrawler {

    private static int MAX_FETCHES = 100000;

    private static boolean running = true;

    private static final int THREAD_POOL_SIZE = 32;
    private final ExecutorService threadPool = Executors
            .newFixedThreadPool(THREAD_POOL_SIZE);
    static FetchResultPersister persister;
    private final String url;
    private final int fetches;

    private SimpleCrawler(String url, int fetches) {
        this.url = url;
        this.fetches = fetches;
    }

    private void process() throws IOException, InterruptedException, ExecutionException {
        final Deque<String> linksToCrawl = new LinkedList<>();
        final HashSet<String> visited = new HashSet<>();
        final CompletionService<Set<String>> completionService = new ExecutorCompletionService<>(
                threadPool);

        long start = System.currentTimeMillis();
        long appStart = start;

        MAX_FETCHES = fetches;
        System.out.println("Set fetches to " + MAX_FETCHES);

        // start the persisting thread
        persister = new FetchResultPersister();
        Thread persisterThread = new Thread(persister);
        persisterThread.start();
        long count = 0L;
        int currentRunningThreads = 0;
        // begin to crawl
        linksToCrawl.offer(url);
        while (true) {
            final String urlToCrawl = linksToCrawl.poll();
            if (urlToCrawl != null) {
                completionService.submit(new FetchThread(urlToCrawl));
                currentRunningThreads++;
                Future<Set<String>> poll;
                if (linksToCrawl.isEmpty()
                        || currentRunningThreads > THREAD_POOL_SIZE) {
                    poll = completionService.take();
                } else {
                    poll = completionService.poll();
                }
                if (poll != null && poll.get() != null) {
                    count++;
                    if (running) {
                        for (String v : poll.get()) {
                            if (visited.add(v))
                                linksToCrawl.offer(v);
                        }
                    }
                }
            } else {
                Thread.sleep(500);
            }

            if (count % 100 == 0) {
                long deltaSeconds = (System.currentTimeMillis() - start) / 1000;
                System.out.println(count + " sites crawled. Took "
                        + deltaSeconds + "s for a 100 element chunk!");
                System.out.println("TP of : " + (100.0f / (float) deltaSeconds)
                        + " sites per second!");
                start = System.currentTimeMillis();
                if (!running) {
                    System.out.println(linksToCrawl.size() + " items left!");
                }
            }
            if (running && linksToCrawl.size() + count > MAX_FETCHES) {
                System.out.println("Crawler received STOP command!");
                running = false;
                System.out.println(linksToCrawl.size()
                        + " sites left to crawl!");
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
            new SimpleCrawler(seedUrl, Integer.valueOf(args[0])).process();
        } else {
            new SimpleCrawler(seedUrl, MAX_FETCHES).process();
        }
    }

}
