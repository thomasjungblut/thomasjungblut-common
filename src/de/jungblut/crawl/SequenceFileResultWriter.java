package de.jungblut.crawl;

import java.io.IOException;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;

/**
 * Writes the result into a sequencefile "files/crawl/result.seq". It tab
 * separates the outlinks in the sequencefile value, the key is the origin url.
 * 
 * @author thomas.jungblut
 * 
 */
public class SequenceFileResultWriter implements ResultWriter<FetchResult> {

  private final Configuration conf;
  private FileSystem fs;

  public SequenceFileResultWriter() throws IOException {
    conf = new Configuration();
    fs = FileSystem.get(conf);
  }

  @Override
  public Writer getWriterInstance() throws IOException {
    return new SequenceFile.Writer(fs, conf, getOutputPath(), Text.class,
        Text.class);
  }

  @Override
  public void write(SequenceFile.Writer writer, FetchResult result)
      throws IOException {
    writer.append(new Text(result.url), asText(result.outlinks));
  }

  @Override
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

}
