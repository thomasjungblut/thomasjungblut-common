package de.jungblut.datastructure;

/**
 * A provider that provides generic input from a generic source as an iterator
 * that can be read over and over again.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class InputProvider<T> {

  /**
   * @return a new iterator to read something. If end of data has been reached,
   *         it can be called to obtain a new iterable that re-reads that data.
   */
  public abstract Iterable<T> iterate();

}
