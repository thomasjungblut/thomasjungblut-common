package de.jungblut.crawl;

import java.util.HashSet;

/**
 * Simple class to contain text and a title of a document.
 * 
 * @author thomas.jungblut
 * 
 */
public class ContentFetchResult extends FetchResult {

  private final String title;
  private final String text;

  public ContentFetchResult(String url, HashSet<String> outlinks) {
    super(url, outlinks);
    title = null;
    text = null;
  }

  public ContentFetchResult(String url, HashSet<String> outlinks, String title,
      String text) {
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

}
