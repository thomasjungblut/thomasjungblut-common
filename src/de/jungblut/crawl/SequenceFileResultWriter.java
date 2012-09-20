package de.jungblut.crawl;

import java.io.IOException;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

/**
 * Writes the result into a sequencefile "files/crawl/result.seq". It tab
 * separates the outlinks in the sequencefile value, the key is the origin url.
 * 
 * @author thomas.jungblut
 * 
 */
public class SequenceFileResultWriter<T extends FetchResult> implements
    ResultWriter<T> {

  private FileSystem fs;
  protected SequenceFile.Writer writer;

  @Override
  public void open(Configuration conf) throws IOException {
    fs = FileSystem.get(conf);
    Path outputPath = getOutputPath();
    fs.delete(outputPath, true);
    writer = new SequenceFile.Writer(fs, conf, outputPath, Text.class,
        Text.class);
  }

  @Override
  public void write(FetchResult result) throws IOException {
    writer.append(new Text(result.url), asText(result.outlinks));
  }

  public Path getOutputPath() {
    return new Path("files/crawl/result.seq");
  }

  private static Text asText(final Set<String> set) {
    Text text = new Text();

    final StringBuilder sb = new StringBuilder();
    for (String s : set) {
      sb.append(s);
      sb.append('\t');
    }
    text.set(sb.toString());
    return text;
  }

  @Override
  public void close() throws Exception {
    writer.close();
  }

}
