package de.jungblut.crawl;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import de.jungblut.crawl.extraction.Extractor;
import de.jungblut.crawl.extraction.OutlinkExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

/**
 * Fast multithreaded crawler, will start a fixed threadpool of 32 threads each
 * will be fed by 10 urls at once. Majorly designed for speed and to use all the
 * available bandwidth. Based on other internet bandwidths, you may retune the
 * parameters of threadpool sizes and how many items should be batched. For my
 * 6k ADSL it works fine by 32 threads batched on 10 urls. You may scale this
 * linearly up, since this class has almost no contention and small sequential
 * code. It is also backed by a bloom filter to check if a URL was visited, so
 * the memory footprint stays low.
 *
 * @author thomas.jungblut
 */
public final class MultithreadedCrawler<T extends FetchResult> implements
        Crawler<T> {

    private static final Logger LOG = LogManager
            .getLogger(MultithreadedCrawler.class);

    private static final int THREAD_POOL_SIZE = 32;
    private static final int BATCH_SIZE = 10;

    private Extractor<T> extractor;
    private FetchResultPersister<T> persister;
    private Thread persisterThread;
    private int fetches = 100000;
    private int poolSize = THREAD_POOL_SIZE;
    private int batchSize = BATCH_SIZE;

    /**
     * Constructs a new Multithreaded Crawler.
     *
     * @param threadPoolSize the number of threads to use.
     * @param batchSize      the number of URLs a batch for a thread should contain.
     * @param fetches        the number of urls to fetch.
     * @param extractor      the extraction logic.
     * @param writer         the writer.
     */
    public MultithreadedCrawler(int threadPoolSize, int batchSize, int fetches,
                                Extractor<T> extractor, ResultWriter<T> writer) throws IOException {
        this.poolSize = threadPoolSize;
        this.batchSize = batchSize;
        setup(fetches, extractor, writer);
    }

    /**
     * Constructs a new Multithreaded Crawler with 32 threads working on 10 url
     * batches at each time.
     *
     * @param fetches   the number of urls to fetch.
     * @param extractor the extraction logic.
     * @param writer    the writer.
     */
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

    @Override
    public final void process(String... seedUrls) throws InterruptedException,
            ExecutionException {
        final Deque<String> linksToCrawl = new LinkedList<>();
        BloomFilter<CharSequence> visited = BloomFilter.create(
                Funnels.stringFunnel(Charset.defaultCharset()), fetches);
        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        final CompletionService<Set<T>> completionService = new ExecutorCompletionService<>(
                threadPool);

        long appStart = System.currentTimeMillis();

        LOG.info("Num sites to fetch " + fetches);

        int currentRunningThreads = 0;
        // seed our to crawl set with the start url
        linksToCrawl.addAll(Arrays.asList(seedUrls));
        for (String seed : seedUrls) {
            visited.put(seed);
        }
        // while we have not fetched enough sites yet
        while (true) {
            // batch together up to 10 items or how much in the list is
            final int length = linksToCrawl.size() > batchSize ? batchSize
                    : linksToCrawl.size();
            // only schedule if we have fetches leftover
            if (fetches > 0 && length > 0) {
                fetches -= length;
                List<String> linkList = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    linkList.add(linksToCrawl.poll());
                }
                // submit a new thread for a batch
                completionService.submit(new FetchThread<>(linkList, extractor));
                currentRunningThreads++;
            }
            // Now we can have a look if other threads have completed yet.
            Future<Set<T>> poll = null;
            if ((linksToCrawl.isEmpty() && currentRunningThreads > 0)
                    || currentRunningThreads > poolSize) {
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
                            if (!visited.mightContain(out)) {
                                // queue them up
                                linksToCrawl.offer(out);
                                visited.put(out);
                            }
                        }
                        persister.add(v);
                    }
                }
            } else {
                // sleep for a second if none completed yet
                Thread.sleep(1000l);
            }
            if (fetches <= 0 && currentRunningThreads == 0) {
                break;
            }
            if (currentRunningThreads == 0 && linksToCrawl.size() == 0) {
                break;
            }
        }

        persister.stop();
        persisterThread.join();
        threadPool.shutdownNow();
        LOG.info("Took overall time of " + (System.currentTimeMillis() - appStart)
                / 1000 + "s.");
    }

    public static void main(String[] args) throws InterruptedException,
            ExecutionException, IOException {
        String seedUrl = "http://news.google.de/";
        new MultithreadedCrawler<>(1000, new OutlinkExtractor(),
                new SequenceFileResultWriter<>()).process(seedUrl);
    }

}
