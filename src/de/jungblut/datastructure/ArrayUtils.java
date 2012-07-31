package de.jungblut.datastructure;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.google.common.base.Preconditions;

/**
 * Array utils for stuff that isn't included in {@link Arrays}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ArrayUtils {

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
   * Finds the occurence of the given key in the given array. Linear search,
   * worst case running time is O(n).
   * 
   * @param array the array to search.
   * @param key the key to search.
   * @return -1 if the key wasn't found nowhere, or the index where the key was
   *         found.
   */
  public static int find(int[] array, int key) {
    Preconditions.checkNotNull(key);
    int position = -1;
    for (int i = 0; i < array.length; i++) {
      if (array[i] == key) {
        position = i;
        break;
      }
    }
    return position;
  }

  /**
   * Finds the occurence of the given key in the given array. Linear search,
   * worst case running time is O(n).
   * 
   * @param array the array to search.
   * @param key the key to search.
   * @return -1 if the key wasn't found nowhere, or the index where the key was
   *         found.
   */
  public static int find(long[] array, long key) {
    Preconditions.checkNotNull(key);
    int position = -1;
    for (int i = 0; i < array.length; i++) {
      if (array[i] == key) {
        position = i;
        break;
      }
    }
    return position;
  }

  /**
   * Finds the occurence of the given key in the given array. Linear search,
   * worst case running time is O(n).
   * 
   * @param array the array to search.
   * @param key the key to search.
   * @return -1 if the key wasn't found nowhere, or the index where the key was
   *         found.
   */
  public static int find(double[] array, double key) {
    Preconditions.checkNotNull(key);
    int position = -1;
    for (int i = 0; i < array.length; i++) {
      if (array[i] == key) {
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

  /**
   * Concats the given arrays.
   * 
   * @param clazz the class type of the array elements.
   * @param arrays the arrays to pass.
   * @return a single array where the given arrays content is concatenated.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] concat(Class<T> clazz, T[]... arrays) {
    int length = 0;
    for (int i = 0; i < arrays.length; i++)
      length += arrays[i].length;
    T[] merged = (T[]) Array.newInstance(clazz, length);
    int index = 0;
    for (int i = 0; i < arrays.length; i++) {
      System.arraycopy(arrays[i], 0, merged, index, arrays[i].length);
      index += arrays[i].length;
    }

    return merged;
  }

  /**
   * Swaps the given indices x with y in the array.
   */
  public static void swap(int[] array, int x, int y) {
    int tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Swaps the given indices x with y in the array.
   */
  public static void swap(long[] array, int x, int y) {
    long tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Swaps the given indices x with y in the array.
   */
  public static void swap(double[] array, int x, int y) {
    double tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Swaps the given indices x with y in the array.
   */
  public static void swap(boolean[] array, int x, int y) {
    boolean tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Swaps the given indices x with y in the array.
   */
  public static <T> void swap(T[] array, int x, int y) {
    T tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Converts the given array of object type to its primitive counterpart.
   */
  public static int[] toPrimitiveArray(Integer[] array) {
    int[] arr = new int[array.length];
    for (int i = 0; i < array.length; i++) {
      Preconditions.checkNotNull(array[i]);
      arr[i] = array[i].intValue();
    }
    return arr;
  }

  /**
   * Converts the given array of object type to its primitive counterpart.
   */
  public static long[] toPrimitiveArray(Long[] array) {
    long[] arr = new long[array.length];
    for (int i = 0; i < array.length; i++) {
      Preconditions.checkNotNull(array[i]);
      arr[i] = array[i].longValue();
    }
    return arr;
  }

  /**
   * Converts the given array of object type to its primitive counterpart.
   */
  public static double[] toPrimitiveArray(Double[] array) {
    double[] arr = new double[array.length];
    for (int i = 0; i < array.length; i++) {
      Preconditions.checkNotNull(array[i]);
      arr[i] = array[i].doubleValue();
    }
    return arr;
  }

  /**
   * Partitions the given array in-place and uses the last element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning.
   */
  public static <T extends Comparable<T>> int partition(T[] array) {
    return partition(array, 0, array.length);
  }

  /**
   * Partitions the given array in-place and uses the end element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning.
   */
  public static <T extends Comparable<T>> int partition(T[] array, int start,
      int end) {
    final int ending = end - 1;
    final T x = array[ending];
    int i = start - 1;
    for (int j = start; j < ending; j++) {
      if (array[j].compareTo(x) < 0) {
        i++;
        swap(array, i, j);
      }
    }
    i++;
    swap(array, i, ending);
    return i;
  }

  /**
   * Partitions the given array in-place and uses the last element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning.
   */
  public static int partition(int[] array) {
    return partition(array, 0, array.length);
  }

  /**
   * Partitions the given array in-place and uses the end element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning.
   */
  public static int partition(int[] array, int start, int end) {
    final int ending = end - 1;
    final int x = array[ending];
    int i = start - 1;
    for (int j = start; j < ending; j++) {
      if (array[j] <= x) {
        i++;
        swap(array, i, j);
      }
    }
    i++;
    swap(array, i, ending);
    return i;
  }

  /**
   * Radix sorts an integer array in O(n). It only works for positive numbers,
   * so please don't come up with negative numbers, it will result in array out
   * of bound exceptions, since they don't have a array index.
   */
  public static void radixSort(int[] a) {
    int[] nPart = new int[2];
    int[][] part = new int[2][a.length];
    for (int i = 0; i < 32; i++) {
      nPart[0] = 0;
      nPart[1] = 0;
      for (int j = 0; j < a.length; j++) {
        int n = (a[j] >> i) & 1;
        part[n][nPart[n]++] = a[j];
      }
      System.arraycopy(part[0], 0, a, 0, nPart[0]);
      System.arraycopy(part[1], 0, a, nPart[0], nPart[1]);
    }
  }

}
