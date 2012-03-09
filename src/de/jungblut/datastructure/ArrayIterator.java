package de.jungblut.datastructure;

import java.util.Iterator;

public final class ArrayIterator<E> implements Iterator<E> {

  private final E[] array;
  private int currentIndex = 0;

  public ArrayIterator(E[] array) {
    this.array = array;
  }

  @Override
  public final boolean hasNext() {
    return currentIndex < array.length;
  }

  @Override
  public final E next() {
    return array[currentIndex++];
  }

  @Override
  public final void remove() {
    array[currentIndex] = null;
  }

}
