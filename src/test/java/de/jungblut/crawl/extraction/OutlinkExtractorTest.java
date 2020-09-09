package de.jungblut.crawl.extraction;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class OutlinkExtractorTest {

    private static final String HOME = "http://people.apache.org/~tjungblut/downloads/test.html";

    @Test
    public void testExtractBaseUrl() {
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
    public void testIsValid() {
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
    public void testFilter() {
        HashSet<String> set = Sets.newHashSet("  ", "\n", "\t", "asdgg");
        HashSet<String> filter = OutlinkExtractor.filter(set,
                Pattern.compile("\\s+"));
        assertEquals(3, filter.size());
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
