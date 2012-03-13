package de.jungblut.crawl.extraction;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.htmlparser.util.ParserException;

import de.jungblut.crawl.ContentFetchResult;
import de.jungblut.crawl.FetchResult;
import de.jungblut.crawl.ResultWriter;
import de.jungblut.crawl.SimpleCrawler;
import de.jungblut.util.Tuple;
import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.LargestContentExtractor;

public class ContentExtractingCrawler implements
    ExtractionLogic<ContentFetchResult> {

  // TODO is this singleton really threadsafe?
  private final BoilerpipeExtractor extractor = LargestContentExtractor
      .getInstance();

  private final Pattern titleExtractor = Pattern
      .compile("<title>(.*?)</title>");

  private final OutlinkExtractor embeddedExtractor = new OutlinkExtractor();

  @Override
  public Set<ContentFetchResult> extract(String site) {

    try {
      Tuple<InputStream, String> connection = embeddedExtractor
          .getConnection(site);
      String html = embeddedExtractor.consumeStream(connection.getFirst(),
          connection.getSecond());
      final HashSet<String> set = embeddedExtractor.extractOutlinks(html);

      Set<FetchResult> resultSet = new HashSet<>(1);
      resultSet.add(new FetchResult(site, set));

      Matcher matcher = titleExtractor.matcher(html);
      boolean foundTitle = matcher.find();
      String title = "";
      if (foundTitle) {
        // TODO why is the regex returning the not grouping as well?
        title = matcher.group().replace("</title>", "").replace("<title>", "");
      }
      String extractedLargestText = extractor.getText(html);

      HashSet<ContentFetchResult> result = new HashSet<>(1);
      result
          .add(new ContentFetchResult(site, set, title, extractedLargestText));
      return result;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (BoilerpipeProcessingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static void main(String[] args) throws IOException,
      InterruptedException, ExecutionException {
    String start = "http://www.cnet.de/tests/entertainment/41550001/nintendo_3ds_im_test_mobile_spielekonsole_mit_3d_display_ohne_brillenpflicht.htm";

    SimpleCrawler<ContentFetchResult> crawler = new SimpleCrawler<ContentFetchResult>(
        start, 2, new ContentExtractingCrawler(), new SystemOutputWriter());

    crawler.process();

  }

  // simple class that persists results and also outputs to console
  static class SystemOutputWriter implements ResultWriter<ContentFetchResult> {

    @Override
    public Path getOutputPath() {
      return new Path("files/crawl/result.seq");
    }

    @Override
    public Writer getWriterInstance() throws IOException {
      Configuration conf = new Configuration();
      return new SequenceFile.Writer(FileSystem.get(conf), conf,
          getOutputPath(), Text.class, Text.class);
    }

    @Override
    public void write(Writer writer, ContentFetchResult result)
        throws IOException {
      System.out.println("Title: " + result.getTitle());
      System.out.println("Text: " + result.getText());
      writer.append(new Text(result.getTitle()), new Text(result.getText()));
    }

  }

}
