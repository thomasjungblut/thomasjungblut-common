package de.jungblut.crawl;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

public interface ResultWriter<T extends FetchResult> {

  public Path getOutputPath();

  public SequenceFile.Writer getWriterInstance() throws IOException;

  public void write(SequenceFile.Writer writer, T result) throws IOException;

}
