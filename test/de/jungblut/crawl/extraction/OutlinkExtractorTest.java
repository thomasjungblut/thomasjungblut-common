package de.jungblut.crawl.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.common.collect.Sets;

import de.jungblut.crawl.FetchResult;

public class OutlinkExtractorTest {

  private static final String HOME = "http://people.apache.org/~tjungblut/downloads/test.html";
  private static final String HOME2 = "http://people.apache.org/~tjungblut/downloads/test2.html";

  @Test
  public void testConsumeStream() throws Exception {
    String res = "<html><head><title>Tutorial:HelloWorld</title></head><body>"
        + "<h1>HelloWorldTutorial</h1></body></html>";

    String consumeStream = OutlinkExtractor.consumeStream(
        new URL(HOME).openStream()).replaceAll("\\s+", "");
    assertEquals(res, consumeStream);
  }

  @Test
  public void testGetConnection() throws IOException {
    @SuppressWarnings("resource")
    InputStream connection = OutlinkExtractor.getConnection(HOME);
    assertNotNull(connection);
    connection.close();
  }

  @Test
  public void testExtractBaseUrl() throws IOException {
    String connection = OutlinkExtractor.extractBaseUrl(HOME);
    assertEquals("http://people.apache.org", connection);

    connection = OutlinkExtractor
        .extractBaseUrl("http://www.berliner-kurier.de/LOL123/omg");
    assertEquals("http://www.berliner-kurier.de", connection);

    connection = OutlinkExtractor
        .extractBaseUrl("http://www.spiegel.de/LOL123/omg");
    assertEquals("http://www.spiegel.de", connection);

    connection = OutlinkExtractor
        .extractBaseUrl("http://www.subdomain.google.de/LOL123/omg");
    assertEquals("http://www.subdomain.google.de", connection);
  }

  @Test
  public void testIsValid() throws Exception {
    assertTrue(OutlinkExtractor.isValid(HOME));
    assertFalse(OutlinkExtractor.isValid(HOME + ".png"));
    assertTrue(OutlinkExtractor
        .isValid("http://www.subdomain.google.de/LOL123/omg"));
    assertTrue(OutlinkExtractor
        .isValid("https://www.subdomain.google.de/LOL123/omg"));
    assertTrue(OutlinkExtractor
        .isValid("https://www.subdomain.google.de/LOL123/omg"));
    assertTrue(OutlinkExtractor
        .isValid("https://www.subdomain-google.de/LOL123/omg"));
    assertFalse(OutlinkExtractor.isValid("http://news.google.de/news/void(0)"));
    assertFalse(OutlinkExtractor.isValid("http://news.google.de/news/void()"));
    assertFalse(OutlinkExtractor.isValid("void(0)"));
    assertFalse(OutlinkExtractor.isValid("void(0);"));
    assertFalse(OutlinkExtractor
        .isValid("http://www.golem.de/news/news@golem.de"));
    assertTrue(OutlinkExtractor
        .isValid("http://handyattacke.de/cgi/websale6.cgi?shopid=etronixx&subshopid=attacke&act=load_tpl&tpl=inc_agbtext_handyattacke.htm"));

  }

  @Test
  public void testFilter() throws Exception {
    HashSet<String> set = Sets.newHashSet("  ", "\n", "\t", "asdgg");
    HashSet<String> filter = OutlinkExtractor.filter(set,
        Pattern.compile("\\s+"));
    assertEquals(3, filter.size());
  }

  @Test
  public void testExtraction() throws Exception {
    OutlinkExtractor mock = new OutlinkExtractor();
    FetchResult extract = mock.extract(HOME2);
    assertEquals(HOME2, extract.getUrl());
    assertEquals(4, extract.getOutlinks().size());
    TreeSet<String> sorted = new TreeSet<>(extract.getOutlinks());
    Iterator<String> it = sorted.iterator();
    assertEquals("http://people.apache.org/local.html", it.next());
    assertEquals("http://people.apache.org/~tjungblut/downloads/local.html",
        it.next());
    // that is the correct html expansion
    assertEquals(
        "http://people.apache.org/~tjungblut/downloads/www.testlol.de/local.html",
        it.next());
    assertEquals("http://www.logs.de/local.html", it.next());

  }

  @Test
  public void testStreamResize() throws Exception {
    // should have a look into some mocking framework :/
    Method resizeMethod = OutlinkExtractor.class.getDeclaredMethod(
        "resizeBuffer", ByteBuffer.class);
    resizeMethod.setAccessible(true);
    ByteBuffer buf = ByteBuffer.allocate(100);
    for (int i = 0; i < 80; i++) {
      buf.put((byte) i);
    }
    buf = (ByteBuffer) resizeMethod.invoke(null, buf);
    assertEquals(100, buf.limit());
    assertEquals(20, buf.remaining());

    // now fill until the 10% threshold and see if the buffer grew
    for (int i = 0; i < 11; i++) {
      buf.put((byte) i);
    }
    buf = (ByteBuffer) resizeMethod.invoke(null, buf);
    assertEquals(200, buf.limit());
    assertEquals(109, buf.remaining());
  }

}
