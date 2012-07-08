package de.jungblut.crawl.extraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import de.jungblut.crawl.FetchResult;
import de.jungblut.math.tuple.Tuple;

public class OutlinkExtractor implements ExtractionLogic<FetchResult> {

  private static final String USER_AGENT_KEY = "User-Agent";
  private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1";
  private static final NodeFilter filter = new NodeClassFilter(LinkTag.class);

  @Override
  public Set<FetchResult> extract(String realUrl) {
    try {

      Tuple<InputStream, String> connection = getConnection(realUrl);
      String html = consumeStream(connection.getFirst(), connection.getSecond());
      final HashSet<String> set = extractOutlinks(html);

      Set<FetchResult> resultSet = new HashSet<>(1);
      resultSet.add(new FetchResult(realUrl, set));

      return resultSet;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return null;
    }
  }

  // opened stream + encoding
  public Tuple<InputStream, String> getConnection(String realUrl)
      throws IOException {
    URL url = new URL(realUrl);
    // maybe we need to write our http connection. DNS cache and
    // inputstream reader...
    /*
     * get the IP of the DNS, put it into the cache. Then open an
     * socketinputstream with an 16k long buffer and let the reader read it...
     */
    URLConnection con = url.openConnection();
    // con.setConnectTimeout(50);
    con.addRequestProperty(USER_AGENT_KEY, USER_AGENT);
    String encoding = con.getContentEncoding();
    if (encoding == null) {
      encoding = "ISO-8859-1";
    }
    return new Tuple<>(con.getInputStream(), encoding);
  }

  /**
   * Method to extract outlinks of a given HTML doc in string.
   */
  public HashSet<String> extractOutlinks(String in) throws ParserException {
    final HashSet<String> set = new HashSet<>();
    Parser parser = new Parser(in);
    parser.setEncoding("UTF-8");
    NodeList matches = parser.extractAllNodesThatMatch(filter);
    SimpleNodeIterator it = matches.elements();
    while (it.hasMoreNodes()) {
      LinkTag node = (LinkTag) it.nextNode();
      String link = node.getLink();
      if (link != null && isValid(link))
        set.add(link);
    }
    return set;
  }

  public String consumeStream(InputStream stream, String encoding) {
    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(stream,
          encoding));
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append('\n');
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        stream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return sb.toString();
  }

  // TODO this can be optimized with a single precompiled regex
  private static boolean isValid(final String s) {
    return !(!s.startsWith("http") || s.matches("javascript:.*|mailto:.*"))
        && !(s.endsWith(".pdf") || s.endsWith(".jpg") || s.endsWith(".png") || s
            .endsWith(".gif"));
  }

}
