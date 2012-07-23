package de.jungblut.datastructure;

import java.util.Arrays;

import com.google.common.base.Preconditions;

/**
 * Array utils for stuff that isn't included in {@link Arrays}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ArrayUtils {

  // TODO implement for all primitive types...
  /**
   * Finds the occurence of the given key in the given array. Linear search,
   * worst case running time is O(n).
   * 
   * @param array the array to search.
   * @param key the key to search.
   * @return -1 if the key wasn't found nowhere, or the index where the key was
   *         found.
   */
  public static <T> int find(T[] array, T key) {
    Preconditions.checkNotNull(key);
    int position = -1;
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(key)) {
        position = i;
        break;
      }
    }
    return position;
  }

  /**
   * Concats the given arrays.
   * 
   * @param arrays the arrays to pass.
   * @return a single array where the given arrays content is concatenated.
   */
  public static int[] concat(int[]... arrays) {
    int length = 0;
    for (int i = 0; i < arrays.length; i++)
      length += arrays[i].length;
    int[] merged = new int[length];
    int index = 0;
    for (int i = 0; i < arrays.length; i++) {
      System.arraycopy(arrays[i], 0, merged, index, arrays[i].length);
      index += arrays[i].length;
    }

    return merged;
  }

}
