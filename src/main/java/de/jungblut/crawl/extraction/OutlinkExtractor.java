package de.jungblut.crawl.extraction;

import de.jungblut.crawl.FetchResult;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Outlink extractor, parses a page just for its outlinks.
 *
 * @author thomas.jungblut
 */
public final class OutlinkExtractor implements Extractor<FetchResult> {

    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final String USER_AGENT_KEY = "User-Agent";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
    private static final NodeFilter LINK_FILTER = new NodeClassFilter(
            LinkTag.class);

    private static final Pattern IGNORE_SUFFIX_PATTERN = Pattern
            .compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|iso|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private static final Pattern BASE_URL_PATTERN = Pattern
            .compile("(http[s]*://[a-z0-9.-]+)");
    private static final Pattern GENERAL_URL_PATTERN = Pattern
            .compile("\\bhttps?://[-a-zA-Z0-9+&#/%?=~_|!:,.;]*[-a-zA-Z0-9+&#/%=~_|]");

    @Override
    public FetchResult extract(String realUrl) {
        if (realUrl == null || !realUrl.startsWith("http")
                || realUrl.length() > 500)
            return null;
        try {

            InputStream connection = getConnection(realUrl);
            String html = consumeStream(connection);

            final HashSet<String> set = extractOutlinks(html, realUrl);

            return new FetchResult(realUrl, set);
        } catch (ParserException pEx) {
            // ignore parser exceptions, they contain mostly garbage
        } catch (RuntimeException rEx) {
            rEx.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.toString().replace("\n", "; ") + " >>> URL was: \""
                    + realUrl + "\"");
        }
        return null;
    }

    /**
     * @return an opened stream.
     */
    public static InputStream getConnection(String realUrl) throws IOException {
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
        return con.getInputStream();
    }

    /**
     * Filters outlinks from a parsed page that NOT matches the given matcher.
     */
    public static HashSet<String> filter(HashSet<String> set, Pattern matcher) {
        if (matcher != null) {
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                if (!matcher.matcher(iterator.next()).matches()) {
                    iterator.remove();
                }
            }
        }
        return set;
    }

    /**
     * Extracts outlinks of the given HTML doc in string.
     *
     * @param html the html to extract the outlinkts from.
     * @param url  the url where we found the current document.
     * @return a set of outlinks.
     */
    public static HashSet<String> extractOutlinks(String html, String url)
            throws ParserException {

        final String baseUrl = extractBaseUrl(url);
        if (baseUrl == null)
            return null;

        final HashSet<String> set = new HashSet<>();
        Parser parser = new Parser(html);
        NodeList matches = parser.extractAllNodesThatMatch(LINK_FILTER);
        SimpleNodeIterator it = matches.elements();
        while (it.hasMoreNodes()) {
            LinkTag node = (LinkTag) it.nextNode();
            String link = node.getLink().trim();
            // remove the anchor if present
            if (link.contains("#")) {
                link = link.substring(0, link.lastIndexOf('#'));
            }
            if (link != null && !link.isEmpty()) {
                if (isValid(link)) {
                    set.add(link);
                    continue;
                }
                // sometimes people adress with "//"
                if (link.startsWith("//")) {
                    link = "http:" + link;
                    if (isValid(link)) {
                        set.add(link);
                        continue;
                    }
                }
                // retry by expanding relative links
                if (link.charAt(0) == '/') {
                    link = baseUrl + link;
                    if (isValid(link)) {
                        set.add(link);
                        continue;
                    }
                }

                // this is a relative url for the current directory
                if (url.endsWith("/")) {
                    link = url + link;
                } else {
                    link = url.substring(0, url.lastIndexOf('/') + 1) + link;
                }
                if (isValid(link)) {
                    set.add(link);
                    continue;
                }
            }
        }
        return set;
    }

    /**
     * Consumes a given {@link InputStream} and returns a string consisting of the
     * html code of the site.
     */
    public static String consumeStream(InputStream stream) throws IOException {
        try {
            UniversalDetector detector = new UniversalDetector(null);
            ReadableByteChannel bc = Channels.newChannel(stream);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            int read = 0;
            while ((read = bc.read(buffer)) != -1) {
                detector.handleData(buffer.array(), buffer.position() - read, read);
                buffer = resizeBuffer(buffer);
            }
            detector.dataEnd();
            // copy the result back to a byte array
            String encoding = detector.getDetectedCharset();
            return new String(buffer.array(), 0, buffer.position(),
                    encoding == null ? "UTF-8" : encoding);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer) {
        ByteBuffer result = buffer;
        // double the size if we have only 10% capacity left
        if (buffer.remaining() < (int) (buffer.capacity() * 0.1f)) {
            result = ByteBuffer.allocate(buffer.capacity() * 2);
            buffer.flip();
            result.put(buffer);
        }
        return result;
    }

    /**
     * Extracts a base url from the given url (to make relative outlinks to
     * absolute ones).
     *
     * @return a base url or null if none was found.
     */
    public static String extractBaseUrl(String url) {
        Matcher matcher = BASE_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Checks if the site does not end with unparsable suffixes likes PDF and if
     * its a valid url by extracting a base url at at index 0.
     */
    public static boolean isValid(final String s) {
        Matcher baseMatcher = BASE_URL_PATTERN.matcher(s);
        return baseMatcher.find() && baseMatcher.start() == 0
                && !IGNORE_SUFFIX_PATTERN.matcher(s).matches()
                && GENERAL_URL_PATTERN.matcher(s).matches();
    }

}
