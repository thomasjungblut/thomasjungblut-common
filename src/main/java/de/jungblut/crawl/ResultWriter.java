package de.jungblut.crawl;

import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

/**
 * Result writing interface. Lifecycle is {@link #open(Configuration)},
 * {@link #write(FetchResult)} n-items and then {@link #close()}. Note that this
 * implements {@link AutoCloseable} and can be used for a try catch with
 * resources.
 *
 * @author thomas.jungblut
 */
public interface ResultWriter<T extends FetchResult> extends AutoCloseable {

    /**
     * Opens the given result writer with a configuration.
     */
    public void open(Configuration conf) throws IOException;

    /**
     * Writes a single item to the output.
     */
    public void write(T result) throws IOException;

}
