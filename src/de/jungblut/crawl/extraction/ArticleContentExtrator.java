package de.jungblut.crawl.extraction;

import static de.jungblut.crawl.extraction.OutlinkExtractor.consumeStream;
import static de.jungblut.crawl.extraction.OutlinkExtractor.extractOutlinks;
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

  // I know I catch hell for parsing HTML with REGEX
  private final Pattern titleExtractor = Pattern
      .compile("<title>(.*?)</title>");

  @Override
  public ContentFetchResult extract(String site) {

    if (site == null || !site.startsWith("http") || site.length() > 500)
      return null;

    try {
      InputStream connection = getConnection(site);
      String html = consumeStream(connection);
      html = StringEscapeUtils.unescapeHtml(html);
      final HashSet<String> outlinkSet = extractOutlinks(html, site);

      Matcher matcher = titleExtractor.matcher(html);
      String title = "";
      if (matcher.find()) {
        title = matcher.group(1);
      }
      String extractedLargestText = extractor.getText(html);
      return new ContentFetchResult(site, outlinkSet, title,
          extractedLargestText);
    } catch (ParserException pEx) {
      // ignore parser exceptions, they contain mostly garbage
    } catch (RuntimeException rEx) {
      rEx.printStackTrace();
    } catch (Exception e) {
      System.err.println(e.toString().replace("\n", "; ") + " >>> URL was: \""
          + site + "\"");
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
