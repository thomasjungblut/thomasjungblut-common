package de.jungblut.crawl;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class FetchResultPersister implements Runnable {

	private final ConcurrentLinkedQueue<FetchResult> queue = new ConcurrentLinkedQueue<FetchResult>();
	private final Configuration conf = new Configuration();
	private SequenceFile.Writer writer = null;
	public boolean running = true;

	public FetchResultPersister() throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path out = new Path("files/crawl/result.seq");
		fs.delete(out, true);
		writer = new SequenceFile.Writer(fs, conf, out, Text.class, Text.class);
	}

	public final void add(final FetchResult result) {
		queue.offer(result);
	}

	@Override
	public final void run() {
		long retrieved = 0L;
		while (running) {
			final FetchResult poll = queue.poll();
			if (poll != null) {
				try {
					writer.append(new Text(poll.url), asText(poll.outlinks));
					retrieved++;
					if (retrieved % 100 == 0) {
						System.out
								.println("Retrieved " + retrieved + " sites!");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Persister received STOP command!");
		int toWrite = queue.size();
		try {
			while (!queue.isEmpty()) {
				final FetchResult poll = queue.poll();
				System.out.println(toWrite--
						+ " results to write before stopping...");
				writer.append(new Text(poll.url), asText(poll.outlinks));
				retrieved++;
				if (retrieved % 100 == 0) {
					System.out.println("Retrieved " + retrieved + " sites!");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Retrieved " + retrieved + " sites in total!");
	}

	private final Text asText(final Set<String> set) {
		Text text = new Text();

		final StringBuilder sb = new StringBuilder();
		for (String s : set) {
			sb.append(s);
			sb.append(";");
		}
		text.set(sb.toString());
		return text;
	}

}
