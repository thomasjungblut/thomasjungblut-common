package de.jungblut.datastructure.trie;

import java.util.ArrayList;

/**
 * Match result based on several documents and a score.
 * 
 * @author thomas.jungblut
 */
public final class MatchResult<T> implements Comparable<MatchResult<T>> {

  public final ArrayList<T> docs;
  public final double score;

  public MatchResult(ArrayList<T> docs, double score) {
    super();
    this.docs = docs;
    this.score = score;
  }

  @Override
  public final String toString() {
    return "MatchResult [" + (docs != null ? "docs=" + docs + ", " : "")
        + "score=" + score + "]";
  }

  @Override
  public int compareTo(MatchResult<T> o) {
    return Double.compare(score, o.score);
  }

}
