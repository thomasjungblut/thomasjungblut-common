package de.jungblut.crawl.extraction;

import static de.jungblut.crawl.extraction.OutlinkExtractor.consumeStream;
import static de.jungblut.crawl.extraction.OutlinkExtractor.extractOutlinks;
import static de.jungblut.crawl.extraction.OutlinkExtractor.filter;
import static de.jungblut.crawl.extraction.OutlinkExtractor.getConnection;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.htmlparser.util.ParserException;

import de.jungblut.crawl.ConsoleResultWriter;
import de.jungblut.crawl.Crawler;
import de.jungblut.crawl.FetchResult;
import de.jungblut.crawl.SequentialCrawler;
import de.jungblut.crawl.extraction.ArticleContentExtrator.ContentFetchResult;
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

  // just parse spiegel.de
  private final Pattern filterPattern = Pattern
      .compile("https?://www.spiegel.de/*");

  @Override
  public final ContentFetchResult extract(String site) {

    if (site == null || !site.startsWith("http") || site.length() > 500)
      return null;

    try {
      Tuple<InputStream, String> connection = getConnection(site);
      String html = consumeStream(connection.getFirst(), connection.getSecond());
      html = StringEscapeUtils.unescapeHtml(html);
      final HashSet<String> outlinkSet = filter(
          extractOutlinks(html, site, connection.getSecond()), filterPattern);

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

  /**
   * Article content fetch result.
   */
  public static class ContentFetchResult extends FetchResult {

    private final String title;
    private final String text;

    public ContentFetchResult(String url, HashSet<String> outlinks) {
      super(url, outlinks);
      title = null;
      text = null;
    }

    public ContentFetchResult(String url, HashSet<String> outlinks,
        String title, String text) {
      super(url, outlinks);
      this.title = title;
      this.text = text;
    }

    public String getTitle() {
      return title;
    }

    public String getText() {
      return text;
    }

    @Override
    public String toString() {
      return title + "\n\n" + text;
    }

  }

  public static void main(String[] args) throws IOException,
      InterruptedException, ExecutionException {
    String start = "http://www.spiegel.de/wissenschaft/natur/erbgut-entziffert-austern-haben-viele-anti-stress-gene-a-856902.html";

    Crawler<ContentFetchResult> crawler = new SequentialCrawler<>(1,
        new ArticleContentExtrator(),
        new ConsoleResultWriter<ContentFetchResult>());

    crawler.process(start);

  }
}
