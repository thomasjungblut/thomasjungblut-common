package de.jungblut.crawl.extraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.util.ParserException;

import de.jungblut.crawl.ContentFetchResult;
import de.jungblut.crawl.Crawler;
import de.jungblut.crawl.STDOUTResultWriter;
import de.jungblut.crawl.SequentialCrawler;
import de.jungblut.math.tuple.Tuple;
import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * Extractor for news articles. Uses Boilerpipes {@link ArticleExtractor} to
 * extract the largest block of text and the article title.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ArticleContentExtrator implements
    Extractor<ContentFetchResult> {

  private final BoilerpipeExtractor extractor = ArticleExtractor.getInstance();

  private final Pattern titleExtractor = Pattern
      .compile("<title>(.*?)</title>");

  private final OutlinkExtractor outlinkExtractor = new OutlinkExtractor();

  @Override
  public final ContentFetchResult extract(String site) {

    if (site == null || !site.startsWith("http") || site.length() > 500)
      return null;

    try {
      Tuple<InputStream, String> connection = outlinkExtractor
          .getConnection(site);
      String html = outlinkExtractor.consumeStream(connection.getFirst(),
          connection.getSecond());
      final HashSet<String> outlinkSet = outlinkExtractor.extractOutlinks(html,
          site);

      Matcher matcher = titleExtractor.matcher(html);
      boolean foundTitle = matcher.find();
      String title = "";
      if (foundTitle) {
        String group = matcher.group();
        // remove the tags from the grouping
        title = group.substring("<title>".length(),
            group.length() - "</title>".length());
      }
      String extractedLargestText = extractor.getText(html);
      return new ContentFetchResult(site, outlinkSet, title,
          extractedLargestText);
    } catch (ParserException pEx) {
      // ignore parser exceptions, they contain mostly garbage
    } catch (Exception e) {
      String errMsg = e.getMessage().length() > 150 ? e.getMessage().substring(
          0, 150) : e.getMessage();
      System.err.println(errMsg.replace("\n", "") + " >>> URL was: \"" + site
          + "\"");
    }

    return null;
  }

  public static void main(String[] args) throws IOException,
      InterruptedException, ExecutionException {
    String start = "http://www.spiegel.de/wirtschaft/service/kartellamt-warnt-vor-kostenexplosion-durch-oekostrom-a-852387.html";

    Crawler<ContentFetchResult> crawler = new SequentialCrawler<>(1,
        new ArticleContentExtrator(), new STDOUTResultWriter());

    crawler.process(start);

  }
}
