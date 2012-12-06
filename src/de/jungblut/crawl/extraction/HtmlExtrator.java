package de.jungblut.crawl.extraction;

import static de.jungblut.crawl.extraction.OutlinkExtractor.consumeStream;
import static de.jungblut.crawl.extraction.OutlinkExtractor.extractOutlinks;
import static de.jungblut.crawl.extraction.OutlinkExtractor.getConnection;

import java.io.InputStream;
import java.util.HashSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.htmlparser.util.ParserException;

import de.jungblut.crawl.FetchResult;
import de.jungblut.crawl.extraction.HtmlExtrator.HtmlFetchResult;
import de.jungblut.math.tuple.Tuple;

/**
 * Extractor for raw html.
 * 
 * @author thomas.jungblut
 * 
 */
public final class HtmlExtrator implements Extractor<HtmlFetchResult> {

  @Override
  public final HtmlFetchResult extract(String site) {

    if (site == null || !site.startsWith("http") || site.length() > 500)
      return null;

    try {
      Tuple<InputStream, String> connection = getConnection(site);
      String html = consumeStream(connection.getFirst(), connection.getSecond());
      html = StringEscapeUtils.unescapeHtml(html);
      final HashSet<String> outlinkSet = extractOutlinks(html, site,
          connection.getSecond());
      return new HtmlFetchResult(site, outlinkSet, html);
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
  public static class HtmlFetchResult extends FetchResult {

    private final String html;

    public HtmlFetchResult(String url, HashSet<String> outlinks) {
      super(url, outlinks);
      html = null;
    }

    public HtmlFetchResult(String url, HashSet<String> outlinks, String html) {
      super(url, outlinks);
      this.html = html;
    }

    public String getHtml() {
      return html;
    }

    @Override
    public String toString() {
      return html;
    }

  }

}
