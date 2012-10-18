package de.jungblut.datastructure;

import java.io.IOException;
import java.lang.reflect.Array;

import org.apache.hadoop.io.Writable;

/**
 * Ring buffer prefetch cache for the {@link DiskList}. You can feed it with a
 * maximum size and the class which to cache.
 * 
 * @author thomas.jungblut
 */
public final class PrefetchCache<E extends Writable> {

  private final DiskList<E> listToCache;
  private final E[] array;
  private int head;
  private int tail;

  /**
   * Creates a prefetch cache. It also fills it for the first time.
   * 
   * @param listToCache the {@link DiskList} to read from.
   * @param clazz the class which to deserialize from the {@link DiskList}.
   * @param size the maximum size to prefetch.
   */
  @SuppressWarnings("unchecked")
  public PrefetchCache(DiskList<E> listToCache, Class<E> clazz, int size)
      throws IOException, InstantiationException, IllegalAccessException {
    this.listToCache = listToCache;
    this.array = (E[]) Array.newInstance(clazz, size);
    for (int i = 0; i < size; i++) {
      array[i] = clazz.newInstance();
    }
    this.head = 0;
    this.tail = 0;
    while (!isFull()) {
      add();
    }
  }

  /**
   * @return a cached item, it always tries to read as much as possible from the
   *         underlying {@link DiskList} so that the cache is always filled. In
   *         case there aren't any new items, the array will be filled with
   *         null.
   */
  public E poll() throws IOException {
    if (head != tail) {
      while (!isFull())
        add();
      E value = array[head++];
      if (head == array.length) {
        head = 0;
      }
      return value;
    } else {
      return null;
    }
  }

  /**
   * Deserializes from the underlying list. Result in the array can be null if
   * there is nothing more to read.
   */
  private void add() throws IOException {
    array[tail] = listToCache.poll(array[tail]);
    tail++;
    if (tail == array.length) {
      tail = 0;
    }
  }

  /**
   * Checks if the current buffer is full.
   * 
   * @return true if full (when the tail is the element before the head, checks
   *         the wrapup as well), or false if none of those apply. There is also
   *         a shortcut if the last read value was null, so it does not try to
   *         read from the underlying storage too much stuff.
   */
  private boolean isFull() {
    return ((head == 0 && tail == (array.length - 1)) || tail + 1 == head || array[tail] == null);
  }

}
