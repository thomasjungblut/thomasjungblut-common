package de.jungblut.datastructure;

import java.util.Iterator;

import de.jungblut.math.tuple.Tuple;

/**
 * Some fancy utilities for iterables, e.G. conversions from and to iterators.
 * 
 * @author thomas.jungblut
 */
public abstract class Iterables {

  /**
   * @return a new iterable that returns the given iterator.
   */
  public static <T> Iterable<T> from(final Iterator<T> iterator) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return iterator;
      }
    };
  }

  /**
   * Consumes the next entries from a parallel interator.
   * 
   * @param it the left iterator.
   * @param it2 the right iterator.
   * @return a tuple. This tuple can have either of its two entries as NULL once
   *         the end of one iterator has been reached. If both iterators are at
   *         their end it simply returns null.
   */
  public static <K, V> Tuple<K, V> consumeNext(final Iterator<K> it,
      final Iterator<V> it2) {
    K nextK = null;
    V nextV = null;
    if (it.hasNext()) {
      nextK = it.next();
    }
    if (it2.hasNext()) {
      nextV = it2.next();
    }
    if (nextK == null && nextV == null) {
      return null;
    } else {
      return new Tuple<>(nextK, nextV);
    }
  }

}
