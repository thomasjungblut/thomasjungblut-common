package de.jungblut.crawl;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;

/**
 * Simple class that persists results into a sequencefile and also outputs to
 * console.
 * 
 * @author thomas.jungblut
 * 
 */
public class STDOUTResultWriter implements ResultWriter<ContentFetchResult> {

  @Override
  public Path getOutputPath() {
    return new Path("files/crawl/result.seq");
  }

  @Override
  public Writer getWriterInstance() throws IOException {
    Configuration conf = new Configuration();
    return new SequenceFile.Writer(FileSystem.get(conf), conf, getOutputPath(),
        Text.class, Text.class);
  }

  @Override
  public void write(Writer writer, ContentFetchResult result)
      throws IOException {
    System.out.println("Title: " + result.getTitle());
    System.out.println("Text: "
        + (result.getText().length() > 50 ? result.getText().substring(0, 50)
            : result.getText()));
    writer.append(new Text(result.getTitle()), new Text(result.getText()));
  }
}
