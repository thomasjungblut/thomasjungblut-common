package de.jungblut.datastructure;

import java.util.Iterator;

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

}
