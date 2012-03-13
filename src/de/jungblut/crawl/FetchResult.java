package de.jungblut.crawl;

import java.util.HashSet;

public class FetchResult {

  final String url;
  final HashSet<String> outlinks;

  public FetchResult(String url, HashSet<String> outlinks) {
    super();
    this.url = url;
    this.outlinks = outlinks;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FetchResult other = (FetchResult) obj;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }

  public String getUrl() {
    return url;
  }

  public HashSet<String> getOutlinks() {
    return outlinks;
  }

}
