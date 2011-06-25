package de.jungblut.crawl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

public class FetchThread implements Callable<Set<String>> {

	private static final String USER_AGENT_KEY = "User-Agent";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1";
	private static final NodeFilter filter = new NodeClassFilter(LinkTag.class);

	private String url;

	public FetchThread(String url) {
		super();
		this.url = url;
	}

	@Override
	public final Set<String> call() throws Exception {
		try {
			final HashSet<String> set = new HashSet<String>();
			URL url = null;
			try {
				url = new URL(this.url);
			} catch (MalformedURLException e) {
				System.out.println(this.url + " is malformed!");
				return null;
			}
			try {
				// maybe we need to write our http connection. DNS cache and
				// inputstream reader...
				/*
				 * get the IP of the DNS, put it into the cache. Then open an
				 * socketinputstream with an 16k long buffer and let the reader
				 * read it...
				 */
				URLConnection con = url.openConnection();
				// con.setConnectTimeout(50);
				con.addRequestProperty(USER_AGENT_KEY, USER_AGENT);
				Parser parser = new Parser(con);
				parser.setEncoding("UTF-8");
				NodeList matches = parser.extractAllNodesThatMatch(filter);
				SimpleNodeIterator it = matches.elements();
				while (it.hasMoreNodes()) {
					LinkTag node = (LinkTag) it.nextNode();
					String link = node.getLink();
					if (link != null && isValid(link))
						set.add(link);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return null;
			}
			SimpleCrawler.persister.add(new FetchResult(this.url, set));
			return set;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private final boolean isValid(final String s) {
		if (!s.startsWith("http") || s.matches("javascript:.*|mailto:.*")) {
			return false;
		} else {
			if(s.endsWith(".pdf") || s.endsWith(".jpg") || s.endsWith(".png") || s.endsWith(".gif"))
				return false;
			return true;
		}
	}

}
