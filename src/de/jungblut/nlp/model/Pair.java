package de.jungblut.nlp.model;

import de.jungblut.math.tuple.Tuple;

/**
 * Pair implementation, unlike {@link Tuple} this implements hashcode and equals
 * on both parts of this pair.
 */
public final class Pair<S, T> {

  private final S first;
  private final T second;

  public Pair(S first, T second) {
    super();
    this.first = first;
    this.second = second;
  }

  public S getFirst() {
    return this.first;
  }

  public T getSecond() {
    return this.second;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.first == null) ? 0 : this.first.hashCode());
    result = prime * result
        + ((this.second == null) ? 0 : this.second.hashCode());
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
    @SuppressWarnings("rawtypes")
    Pair other = (Pair) obj;
    if (this.first == null) {
      if (other.first != null)
        return false;
    } else if (!this.first.equals(other.first))
      return false;
    if (this.second == null) {
      if (other.second != null)
        return false;
    } else if (!this.second.equals(other.second))
      return false;
    return true;
  }

}
