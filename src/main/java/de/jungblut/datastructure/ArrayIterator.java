package de.jungblut.datastructure;

import java.util.Iterator;

/**
 * Generic ArrayIterator.
 *
 * @author thomas.jungblut
 */
public final class ArrayIterator<E> implements Iterator<E> {

    private final E[] array;
    private int currentIndex = 0;

    /**
     * Get a new ArrayIterator for the given array.
     */
    public ArrayIterator(E[] array) {
        this.array = array;
    }

    /**
     * Checks if the iterator has something to iterate by checking the current
     * iteration index against the array length.
     *
     * @return true if there is a next item or false if not.
     */
    @Override
    public final boolean hasNext() {
        return currentIndex < array.length;
    }

    /**
     * @return the next entry in the array.
     */
    @Override
    public final E next() {
        return array[currentIndex++];
    }

    /**
     * @return the current index of the last "nexted" item.
     */
    public final int getIndex() {
        return currentIndex - 1;
    }

    /**
     * Removes the current item from the list.
     */
    @Override
    public final void remove() {
        if (currentIndex >= 0 && currentIndex <= array.length) {
            array[currentIndex - 1] = null;
        }
    }

}
