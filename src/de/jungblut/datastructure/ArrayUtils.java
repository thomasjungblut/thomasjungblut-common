package de.jungblut.datastructure;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;

/**
 * Array utils for stuff that isn't included in {@link Arrays}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ArrayUtils {

  private ArrayUtils() {
    throw new IllegalAccessError();
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
   * Concats the given arrays.
   * 
   * @param arrays the arrays to pass.
   * @return a single array where the given arrays content is concatenated.
   */
  public static int[] concat(int[]... arrays) {
    int length = 0;
    for (int[] array1 : arrays)
      length += array1.length;
    int[] merged = new int[length];
    int index = 0;
    for (int[] array : arrays) {
      System.arraycopy(array, 0, merged, index, array.length);
      index += array.length;
    }

    return merged;
  }

  /**
   * Concats the given arrays.
   * 
   * @param arrays the arrays to pass.
   * @return a single array where the given arrays content is concatenated.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] concat(T[]... arrays) {
    if (arrays.length > 0) {
      int length = 0;
      for (T[] array1 : arrays)
        length += array1.length;

      T[] merged = (T[]) Array.newInstance(arrays[0].getClass()
          .getComponentType(), length);
      int index = 0;
      for (T[] array : arrays) {
        System.arraycopy(array, 0, merged, index, array.length);
        index += array.length;
      }

      return merged;
    } else {
      return null;
    }
  }

  /**
   * Copies the given array into a new one.
   */
  public static int[] copy(int[] array) {
    int[] newInt = new int[array.length];
    System.arraycopy(array, 0, newInt, 0, array.length);
    return newInt;
  }

  /**
   * Copies the given array into a new one.
   */
  public static double[] copy(double[] array) {
    double[] newInt = new double[array.length];
    System.arraycopy(array, 0, newInt, 0, array.length);
    return newInt;
  }

  /**
   * Copies the given array into a new one.
   */
  public static long[] copy(long[] array) {
    long[] newInt = new long[array.length];
    System.arraycopy(array, 0, newInt, 0, array.length);
    return newInt;
  }

  /**
   * Copies the given array into a new one.
   */
  public static <T> T[] copy(T[] array) {
    return Arrays.copyOf(array, array.length);
  }

  /**
   * Swaps the given index x with y in the array.
   */
  public static void swap(int[] array, int x, int y) {
    int tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Swaps the given index x with y in the array.
   */
  public static void swap(long[] array, int x, int y) {
    long tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Swaps the given index x with y in the array.
   */
  public static void swap(double[] array, int x, int y) {
    double tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Swaps the given index x with y in the array.
   */
  public static void swap(boolean[] array, int x, int y) {
    boolean tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Swaps the given index x with y in the array.
   */
  public static <T> void swap(T[] array, int x, int y) {
    T tmpIndex = array[x];
    array[x] = array[y];
    array[y] = tmpIndex;
  }

  /**
   * Converts the given list of object type to its primitive counterpart.
   */
  public static int[] toPrimitiveArray(List<Integer> list) {
    int[] arr = new int[list.size()];
    int index = 0;
    for (Integer i : list) {
      Preconditions.checkNotNull(i);
      arr[index++] = i.intValue();
    }
    return arr;
  }

  /**
   * Converts the given array of object type to its primitive counterpart.
   */
  public static int[] toPrimitiveArray(Integer[] array) {
    int[] arr = new int[array.length];
    for (int i = 0; i < array.length; i++) {
      Preconditions.checkNotNull(array[i]);
      arr[i] = array[i];
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
      arr[i] = array[i];
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
      arr[i] = array[i];
    }
    return arr;
  }

  /**
   * Converts the given int array to a list of object wrappers.
   */
  public static List<Integer> toObjectList(int[] array) {
    ArrayList<Integer> lst = new ArrayList<>(array.length);
    for (int x : array)
      lst.add(x);
    return lst;
  }

  /**
   * Converts the given long array to a list of object wrappers.
   */
  public static List<Long> toObjectList(long[] array) {
    ArrayList<Long> lst = new ArrayList<>(array.length);
    for (long x : array)
      lst.add(x);
    return lst;
  }

  /**
   * Converts the given double array to a list of object wrappers.
   */
  public static List<Double> toObjectList(double[] array) {
    ArrayList<Double> lst = new ArrayList<>(array.length);
    for (double x : array)
      lst.add(x);
    return lst;
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
   * Partitions the given array in-place and uses the last element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning.
   */
  public static int partition(long[] array) {
    return partition(array, 0, array.length);
  }

  /**
   * Partitions the given array in-place and uses the end element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning.
   */
  public static int partition(long[] array, int start, int end) {
    final int ending = end - 1;
    final long x = array[ending];
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
   * Partitions the given array in-place and uses the last element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning.
   */
  public static int partition(double[] array) {
    return partition(array, 0, array.length);
  }

  /**
   * Partitions the given array in-place and uses the end element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning.
   */
  public static int partition(double[] array, int start, int end) {
    final int ending = end - 1;
    final double x = array[ending];
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
   * Selects the kth smallest element in the array in linear time, if the array
   * is smaller than or equal to 10 a radix sort will be used and the kth
   * element will be returned. So k = 1, will return the absolutely smallest
   * element.
   * 
   * @return the kth smallest index of the element.
   */
  public static int quickSelect(int[] array, int k) {
    Preconditions.checkArgument(k > 0 && k <= array.length);
    final int n = array.length;
    if (n <= 10) {
      radixSort(array);
      return k - 1;
    }
    return quickSelect(array, 0, n, k);
  }

  /**
   * Selects the kth smallest element in the array.
   * 
   * @param start the index where to start.
   * @param end the index where to end.
   * @return the kth smallest index of the element.
   */
  public static int quickSelect(int[] array, int start, int end, int k) {
    if (start == end) {
      return start;
    }

    final int pivot = partition(array, start, end);
    final int length = pivot - start + 1;

    if (length == k) {
      return pivot;
    } else if (k < length) {
      return quickSelect(array, start, pivot - 1, k);
    } else {
      return quickSelect(array, pivot + 1, end, k - length);
    }
  }

  /**
   * Selects the kth smallest element in the array in linear time. k = 1, will
   * return the absolutely smallest element.
   * 
   * @return the kth smallest index of the element.
   */
  public static int quickSelect(double[] array, int k) {
    Preconditions.checkArgument(k > 0 && k <= array.length);
    return quickSelect(array, 0, array.length, k);
  }

  /**
   * Selects the kth smallest element in the array.
   * 
   * @param start the index where to start.
   * @param end the index where to end.
   * @return the kth smallest index of the element.
   */
  public static int quickSelect(double[] array, int start, int end, int k) {
    if (start == end) {
      return start;
    }

    final int pivot = partition(array, start, end);
    final int length = pivot - start + 1;

    if (length == k) {
      return pivot;
    } else if (k < length) {
      return quickSelect(array, start, pivot - 1, k);
    } else {
      return quickSelect(array, pivot + 1, end, k - length);
    }
  }

  /**
   * Selects the kth smallest element in the array in linear time. k = 1, will
   * return the absolutely smallest element.
   * 
   * @return the kth smallest index of the element.
   */
  public static int quickSelect(long[] array, int k) {
    Preconditions.checkArgument(k > 0 && k <= array.length);
    return quickSelect(array, 0, array.length, k);
  }

  /**
   * Selects the kth smallest element in the array.
   * 
   * @param start the index where to start.
   * @param end the index where to end.
   * @return the kth smallest index of the element.
   */
  public static int quickSelect(long[] array, int start, int end, int k) {
    if (start == end) {
      return start;
    }

    final int pivot = partition(array, start, end);
    final int length = pivot - start + 1;

    if (length == k) {
      return pivot;
    } else if (k < length) {
      return quickSelect(array, start, pivot - 1, k);
    } else {
      return quickSelect(array, pivot + 1, end, k - length);
    }
  }

  /**
   * Selects the kth smallest element in the array in linear time. k = 1, will
   * return the absolutely smallest element.
   * 
   * @return the kth smallest index of the element.
   */
  public static <T extends Comparable<T>> int quickSelect(T[] array, int k) {
    Preconditions.checkArgument(k > 0 && k <= array.length);
    return quickSelect(array, 0, array.length, k);
  }

  /**
   * Selects the kth smallest element in the array.
   * 
   * @param start the index where to start.
   * @param end the index where to end.
   * @return the kth smallest index of the element.
   */
  public static <T extends Comparable<T>> int quickSelect(T[] array, int start,
      int end, int k) {
    if (start == end) {
      return start;
    }

    final int pivot = partition(array, start, end);
    final int length = pivot - start + 1;

    if (length == k) {
      return pivot;
    } else if (k < length) {
      return quickSelect(array, start, pivot - 1, k);
    } else {
      return quickSelect(array, pivot + 1, end, k - length);
    }
  }

  /**
   * Finds the median of medians in the given array.
   * 
   * @return the index of the median of medians.
   */
  public static int medianOfMedians(int[] array) {
    final int splitSize = array.length / 5;

    if (splitSize <= 2) {
      radixSort(array);
      return array[array.length / 2];
    }

    int[] pivots = new int[splitSize];
    for (int i = 0; i < splitSize; i++) {
      final int start = i * 5;
      final int end = i * 5 + 5;
      pivots[i] = partition(array, start, end);
    }

    return pivots[splitSize / 2];
  }

  /**
   * Creates an integer array from the given start up to a end number with a
   * stepsize.
   * 
   * @param from the integer to start with.
   * @param to the integer to end with.
   * @param stepsize the stepsize to take
   * @return an integer array from start to end incremented by stepsize.
   */
  public static int[] fromUpTo(int from, int to, int stepsize) {
    int[] v = new int[(to - from) / stepsize];

    for (int i = 0; i < v.length; i++) {
      v[i] = from + i * stepsize;
    }
    return v;
  }

  /**
   * Creates a long array from the given start up to a end number with a
   * stepsize.
   * 
   * @param from the long to start with.
   * @param to the long to end with.
   * @param stepsize the stepsize to take
   * @return a long array from start to end incremented by stepsize.
   */
  public static long[] fromUpTo(long from, long to, long stepsize) {
    long[] v = new long[(int) ((to - from) / stepsize)];

    for (int i = 0; i < v.length; i++) {
      v[i] = from + i * stepsize;
    }
    return v;
  }

  /**
   * Creates a double array from the given start up to a end number with a
   * stepsize.
   * 
   * @param from the double to start with.
   * @param to the double to end with.
   * @param stepsize the stepsize to take
   * @return a double array from start to end incremented by stepsize.
   */
  public static double[] fromUpTo(double from, double to, double stepsize) {
    double[] v = new double[(int) (Math.round(((to - from) / stepsize) + 0.5))];

    for (int i = 0; i < v.length; i++) {
      v[i] = from + i * stepsize;
    }
    return v;
  }

  /**
   * Radix sorts an integer array in O(m*n), where m is the length of the key
   * (here 32 bit) and n the number of elements. It only works for positive
   * numbers, so please don't come up with negative numbers, it will result in
   * array out of bound exceptions, since they don't have an array index.
   */
  public static void radixSort(int[] a) {
    int[] nPart = new int[2];
    int[][] part = new int[2][a.length];
    for (int i = 0; i < 32; i++) {
      nPart[0] = 0;
      nPart[1] = 0;
      for (int anA : a) {
        int n = (anA >> i) & 1;
        part[n][nPart[n]++] = anA;
      }
      System.arraycopy(part[0], 0, a, 0, nPart[0]);
      System.arraycopy(part[1], 0, a, nPart[0], nPart[1]);
    }
  }

  /**
   * Counting sort that sorts the integer array in O(n+k) where n is the number
   * of elements and k is the length of the integer intervals given (high -
   * low). So you can imagine that it uses domain knowledge of the contained
   * integers, like the lowest value and the highest. It only works for positive
   * numbers, so please don't come up with negative numbers, it will result in
   * array out of bound exceptions, since they don't have an array index.
   */
  public static void countingSort(int[] a, int low, int high) {
    final int[] counts = new int[high - low + 1];
    for (int x : a) {
      counts[x - low]++;
    }

    int current = 0;
    for (int i = 0; i < counts.length; i++) {
      Arrays.fill(a, current, current + counts[i], i + low);
      current += counts[i];
    }
  }

  /**
   * Quicksorts this array.
   */
  public static void quickSort(int[] a) {
    quickSort(a, 0, a.length);
  }

  /**
   * Quicksorts this array. With offset and length, this will be recursively
   * called by itself.
   */
  public static void quickSort(int[] a, int offset, int length) {
    if (offset < length) {
      int pivot = partition(a, offset, length);
      quickSort(a, offset, pivot);
      quickSort(a, pivot + 1, length);
    }
  }

  /**
   * Quicksorts this array.
   */
  public static void quickSort(long[] a) {
    quickSort(a, 0, a.length);
  }

  /**
   * Quicksorts this array. With offset and length, this will be recursively
   * called by itself.
   */
  public static void quickSort(long[] a, int offset, int length) {
    if (offset < length) {
      int pivot = partition(a, offset, length);
      quickSort(a, offset, pivot);
      quickSort(a, pivot + 1, length);
    }
  }

  /**
   * Quicksorts this array.
   */
  public static void quickSort(double[] a) {
    quickSort(a, 0, a.length);
  }

  /**
   * Quicksorts this array. With offset and length, this will be recursively
   * called by itself.
   */
  public static void quickSort(double[] a, int offset, int length) {
    if (offset < length) {
      int pivot = partition(a, offset, length);
      quickSort(a, offset, pivot);
      quickSort(a, pivot + 1, length);
    }
  }

  /**
   * Quicksorts this array.
   */
  public static <T extends Comparable<T>> void quickSort(T[] a) {
    quickSort(a, 0, a.length);
  }

  /**
   * Quicksorts this array. With offset and length, this will be recursively
   * called by itself.
   */
  public static <T extends Comparable<T>> void quickSort(T[] a, int offset,
      int length) {
    if (offset < length) {
      int pivot = partition(a, offset, length);
      quickSort(a, offset, pivot);
      quickSort(a, pivot + 1, length);
    }
  }

  /**
   * Multi-sorts the given arrays with the quicksort algorithm. It assumes that
   * all arrays have the same sizes and it sorts on the first dimension of these
   * arrays. If the given arrays are null or empty, it will do nothing, if just
   * a single array was passed it will sort it via {@link Arrays} sort;
   */
  public static void multiQuickSort(int[]... arrays) {
    multiQuickSort(0, arrays);
  }

  /**
   * Multi-sorts the given arrays with the quicksort algorithm. It assumes that
   * all arrays have the same sizes and it sorts on the given dimension index
   * (starts with 0) of these arrays. If the given arrays are null or empty, it
   * will do nothing, if just a single array was passed it will sort it via
   * {@link Arrays} sort;
   */
  public static void multiQuickSort(int sortDimension, int[]... arrays) {
    // check if the lengths are equal, break if everything is empty
    if (arrays == null || arrays.length == 0) {
      return;
    }
    // if the array only has a single dimension, sort it and return
    if (arrays.length == 1) {
      Arrays.sort(arrays[0]);
      return;
    }
    // also return if the sort dimension is not in our array range
    if (sortDimension < 0 || sortDimension >= arrays.length) {
      return;
    }
    // check sizes
    int firstArrayLength = arrays[0].length;
    for (int i = 1; i < arrays.length; i++) {
      if (arrays[i] == null || firstArrayLength != arrays[i].length)
        return;
    }

    multiQuickSort(arrays, 0, firstArrayLength, sortDimension);
  }

  /**
   * Internal multi quicksort, doing the real algorithm.
   */
  private static void multiQuickSort(int[][] a, int offset, int length,
      int indexToSort) {
    if (offset < length) {
      int pivot = multiPartition(a, offset, length, indexToSort);
      multiQuickSort(a, offset, pivot, indexToSort);
      multiQuickSort(a, pivot + 1, length, indexToSort);
    }
  }

  /**
   * Partitions the given array in-place and uses the end element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning. This is a multi way partitioning algorithm, you
   * have to provide a partition array index to know which is the array that
   * needs to be partitioned. The swap operations are applied on the other
   * elements as well.
   */
  private static int multiPartition(int[][] array, int start, int end,
      int partitionArrayIndex) {
    final int ending = end - 1;
    final int x = array[partitionArrayIndex][ending];
    int i = start - 1;
    for (int j = start; j < ending; j++) {
      if (array[partitionArrayIndex][j] <= x) {
        i++;
        for (int[] anArray : array) {
          swap(anArray, i, j);
        }
      }
    }
    i++;
    for (int[] anArray : array) {
      swap(anArray, i, ending);
    }

    return i;
  }

  /**
   * Multi-sorts the given arrays with the quicksort algorithm. It assumes that
   * all arrays have the same sizes and it sorts on the first dimension of these
   * arrays. If the given arrays are null or empty, it will do nothing, if just
   * a single array was passed it will sort it via {@link Arrays} sort;
   */
  @SafeVarargs
  public static <T extends Comparable<T>> void multiQuickSort(T[]... arrays) {
    multiQuickSort(0, arrays);
  }

  /**
   * Multi-sorts the given arrays with the quicksort algorithm. It assumes that
   * all arrays have the same sizes and it sorts on the given dimension index
   * (starts with 0) of these arrays. If the given arrays are null or empty, it
   * will do nothing, if just a single array was passed it will sort it via
   * {@link Arrays} sort;
   */
  @SafeVarargs
  public static <T extends Comparable<T>> void multiQuickSort(
      int sortDimension, T[]... arrays) {
    // check if the lengths are equal, break if everything is empty
    if (arrays == null || arrays.length == 0) {
      return;
    }
    // if the array only has a single dimension, sort it and return
    if (arrays.length == 1) {
      Arrays.sort(arrays[0]);
      return;
    }
    // also return if the sort dimension is not in our array range
    if (sortDimension < 0 || sortDimension >= arrays.length) {
      return;
    }
    // check sizes
    int firstArrayLength = arrays[0].length;
    for (int i = 1; i < arrays.length; i++) {
      if (arrays[i] == null || firstArrayLength != arrays[i].length)
        return;
    }

    multiQuickSort(arrays, 0, firstArrayLength, sortDimension);
  }

  /**
   * Internal multi quicksort, doing the real algorithm.
   */
  private static <T extends Comparable<T>> void multiQuickSort(T[][] a,
      int offset, int length, int indexToSort) {
    if (offset < length) {
      int pivot = multiPartition(a, offset, length, indexToSort);
      multiQuickSort(a, offset, pivot, indexToSort);
      multiQuickSort(a, pivot + 1, length, indexToSort);
    }
  }

  /**
   * Partitions the given array in-place and uses the end element as pivot,
   * everything less than the pivot will be placed left and everything greater
   * will be placed right of the pivot. It returns the index of the pivot
   * element after partitioning. This is a multi way partitioning algorithm, you
   * have to provide a partition array index to know which is the array that
   * needs to be partitioned. The swap operations are applied on the other
   * elements as well.
   */
  private static <T extends Comparable<T>> int multiPartition(T[][] array,
      int start, int end, int partitionArrayIndex) {
    final int ending = end - 1;
    final T x = array[partitionArrayIndex][ending];
    int i = start - 1;
    for (int j = start; j < ending; j++) {
      if (array[partitionArrayIndex][j].compareTo(x) < 0) {
        i++;
        for (T[] anArray : array) {
          swap(anArray, i, j);
        }
      }
    }
    i++;
    for (T[] anArray : array) {
      swap(anArray, i, ending);
    }

    return i;
  }

  /**
   * Deduplicates an array in linear time, it does not change the order of the
   * elements.
   */
  public static int[] deduplicate(int[] arr) {
    if (arr.length <= 1)
      return arr;
    TIntArrayList list = new TIntArrayList();
    TIntHashSet set = new TIntHashSet();

    for (int a : arr) {
      if (set.add(a)) {
        list.add(a);
      }
    }

    return list.toArray();
  }

  /**
   * Deduplicates an array in linear time, it does not change the order of the
   * elements. Note that equals and hashcode must be overridden for this to
   * work.
   */
  public static <T> ArrayList<T> deduplicate(T[] arr) {
    ArrayList<T> list = new ArrayList<>();
    HashSet<T> set = new HashSet<>();

    for (T a : arr) {
      if (set.add(a)) {
        list.add(a);
      }
    }
    return list;
  }

  /**
   * Computes the union of two arrays.
   */
  public static int[] union(int[] a, int[] b) {
    TIntHashSet set = new TIntHashSet();
    set.addAll(a);
    set.addAll(b);
    return set.toArray();
  }

  /**
   * Computes the intersection of two <b>sorted</b> arrays. Will deduplicate the
   * items, so the return is a set of integers.
   */
  public static int[] intersection(int[] arr, int[] arr2) {
    TIntArrayList lst = new TIntArrayList();
    int i = 0, j = 0;
    while (i < arr.length && j < arr2.length) {
      if (arr[i] == arr2[j]) {
        // only add if the last element we've added wasn't included yet
        if (lst.isEmpty() || lst.get(lst.size() - 1) < arr[i])
          lst.add(arr[i]);
        i++;
        j++;
      } else if (arr[i] > arr2[j]) {
        j++;
      } else {
        i++;
      }
    }

    return lst.toArray();
  }

  /**
   * Computes the intersection of two <b>unsorted</b> arrays. Will deduplicate
   * the items, so the return is a set of integers.
   */
  public static int[] intersectionUnsorted(int[] arr, int[] arr2) {
    TIntHashSet set = new TIntHashSet();
    TIntHashSet toReturn = new TIntHashSet();
    for (int a : arr) {
      set.add(a);
    }

    for (int a : arr2) {
      if (set.contains(a)) {
        toReturn.add(a);
      }
    }

    return toReturn.toArray();
  }

  /**
   * If array contains unique integers in a range between 0 and n-1, this
   * function finds the only one missing in linear time and constant memory.
   * This is more of a typical homework or interview question than of actually
   * use. <br/>
   * The trick is to sum the items in the array and then calculate the expected
   * sum of the elements, then diff and return the value.
   */
  public static int missingNumber(int[] array) {
    int sum = 0;
    for (int x : array) {
      sum += x;
    }

    return ((array.length) * (array.length + 1) / 2) - sum;
  }

  /**
   * @return the min value in this array,
   */
  public static int min(int[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    int minValue = array[0];
    for (int aVector : array) {
      if (minValue > aVector) {
        minValue = aVector;
      }
    }
    return minValue;
  }

  /**
   * @return the minimum index in this array,
   */
  public static int minIndex(int[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    int minIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (array[minIndex] > array[i]) {
        minIndex = i;
      }
    }
    return minIndex;
  }

  /**
   * @return the minimum value in this array,
   */
  public static long min(long[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    long minValue = array[0];
    for (long aVector : array) {
      if (minValue > aVector) {
        minValue = aVector;
      }
    }
    return minValue;
  }

  /**
   * @return the minimum index in this array,
   */
  public static int minIndex(long[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    int minIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (array[minIndex] > array[i]) {
        minIndex = i;
      }
    }
    return minIndex;
  }

  /**
   * @return the minimum value in this array,
   */
  public static double min(double[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    double minValue = array[0];
    for (double aVector : array) {
      if (minValue > aVector) {
        minValue = aVector;
      }
    }
    return minValue;
  }

  /**
   * @return the minimum index in this array,
   */
  public static int minIndex(double[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    int minIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (array[minIndex] > array[i]) {
        minIndex = i;
      }
    }
    return minIndex;
  }

  /**
   * @return the maximum value in this array,
   */
  public static int max(int[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    int maxValue = array[0];
    for (int aVector : array) {
      if (maxValue < aVector) {
        maxValue = aVector;
      }
    }
    return maxValue;
  }

  /**
   * @return the maximum index in this array,
   */
  public static int maxIndex(int[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    int maxIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (array[maxIndex] < array[i]) {
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  /**
   * @return the maximum value in this array,
   */
  public static long max(long[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    long maxValue = array[0];
    for (long aVector : array) {
      if (maxValue < aVector) {
        maxValue = aVector;
      }
    }
    return maxValue;
  }

  /**
   * @return the maximum index in this array,
   */
  public static int maxIndex(long[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    int maxIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (array[maxIndex] < array[i]) {
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  /**
   * @return the maximum value in this array,
   */
  public static double max(double[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    double maxValue = array[0];
    for (double aVector : array) {
      if (maxValue < aVector) {
        maxValue = aVector;
      }
    }
    return maxValue;
  }

  /**
   * @return the maximum index in this array,
   */
  public static int maxIndex(double[] array) {
    Preconditions.checkNotNull(array, "array must not be null");
    Preconditions.checkArgument(array.length > 0, "array must not be empty");
    int maxIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (array[maxIndex] < array[i]) {
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  /**
   * Splits the given array from 0 to the given splitindex (included).
   * 
   * @return a new array with the same objects in array[0..splitIndex]
   */
  public static <T> T[] subArray(T[] array, int splitIndex) {
    return subArray(array, 0, splitIndex);
  }

  /**
   * Splits the given array from the given startIndex to the given splitIndex
   * (included).
   * 
   * @return a new array with the same objects in array[startIndex..splitIndex]
   */
  public static <T> T[] subArray(T[] array, int startIndex, int splitIndex) {
    @SuppressWarnings("unchecked")
    T[] subArray = (T[]) Array.newInstance(array.getClass().getComponentType(),
        splitIndex - startIndex + 1);

    System.arraycopy(array, startIndex, subArray, 0, subArray.length);

    return subArray;
  }

  /**
   * Shuffles the given array.
   * 
   * @return mutated parameter array that was shuffled beforehand.
   */
  public static <T> T[] shuffle(T[] array) {
    return shuffle(array, new Random());
  }

  /**
   * Shuffles the given array with the given random function.
   * 
   * @return mutated parameter array that was shuffled beforehand.
   */
  public static <T> T[] shuffle(T[] array, Random rnd) {
    for (int i = array.length; i > 1; i--)
      swap(array, i - 1, rnd.nextInt(i));
    return array;
  }

  /**
   * Shuffles the given array.
   * 
   * @return mutated parameter array that was shuffled beforehand.
   */
  @SafeVarargs
  public static <T> T[] multiShuffle(T[] array, T[]... additions) {
    return multiShuffle(array, new Random(), additions);
  }

  /**
   * Shuffles the given array with the given random function.
   * 
   * @return mutated parameter array that was shuffled beforehand.
   */
  @SafeVarargs
  public static <T> T[] multiShuffle(T[] array, Random rnd, T[]... additions) {
    for (int i = array.length; i > 1; i--) {
      final int swapIndex = rnd.nextInt(i);
      swap(array, i - 1, swapIndex);
      for (T[] arr : additions) {
        swap(arr, i - 1, swapIndex);
      }
    }
    return array;
  }

  /**
   * Creates the given array from a varargs parameter.
   * 
   * @param arrays the array to create.
   * @return the inputted stuff as array.
   */
  public static int[] create(int... arrays) {
    return arrays;
  }

  /**
   * Creates the given array from a varargs parameter.
   * 
   * @param arrays the array to create.
   * @return the inputted stuff as array.
   */
  public static long[] create(long... arrays) {
    return arrays;
  }

  /**
   * Creates the given array from a varargs parameter.
   * 
   * @param arrays the array to create.
   * @return the inputted stuff as array.
   */
  public static double[] create(double... arrays) {
    return arrays;
  }

  /**
   * Creates the given array from a varargs parameter.
   * 
   * @param arrays the array to create.
   * @return the inputted stuff as array.
   */
  public static byte[] create(byte... arrays) {
    return arrays;
  }

  /**
   * Creates the given array from a varargs parameter.
   * 
   * @param arrays the array to create.
   * @return the inputted stuff as array.
   */
  @SafeVarargs
  public static <T> T[] create(T... arrays) {
    return arrays;
  }

  /**
   * Merges two sorted arrays to a single new array.
   * 
   * @param a sorted array.
   * @param b sorted array.
   * @return a new array that merged both into a new sorted array.
   */
  public static int[] merge(int[] a, int[] b) {
    int[] toReturn = new int[a.length + b.length];
    int i = 0, j = 0, k = 0;
    while (i < a.length && j < b.length) {
      if (a[i] < b[j]) {
        toReturn[k] = a[i];
        i++;
      } else {
        toReturn[k] = b[j];
        j++;
      }
      k++;
    }

    System.arraycopy(a, i, toReturn, k, a.length - i);
    System.arraycopy(b, j, toReturn, k + a.length - i, b.length - j);

    return toReturn;
  }

  /**
   * Merges two sorted subparts of the given number array. E.G: if you want to
   * merge { 1, 2, 5, 3, 5, 6, 7 } you have to pass merge(concat, 0, 2, 6),
   * because you are starting at zero, the second sorted array begins at index
   * 3, so it is 3-1=2. The end is the length of the array - 1.
   * 
   * @param numbers the array which has two sorted sub arrays.
   * @param startIndexA the start index of the first sorted array.
   * @param endIndexA the end index of the first sorted array.
   * @param endIndexB the end of the second array.
   */
  public static void merge(int[] numbers, int startIndexA, int endIndexA,
      int endIndexB) {
    int[] toReturn = new int[endIndexB - startIndexA + 1];
    int i = 0, k = startIndexA, j = endIndexA + 1;
    while (i < toReturn.length) {
      if (numbers[k] < numbers[j]) {
        toReturn[i] = numbers[k];
        k++;
      } else {
        toReturn[i] = numbers[j];
        j++;
      }
      i++;
      // if we hit the limit of an array, copy the rest
      if (j > endIndexB) {
        System.arraycopy(numbers, k, toReturn, i, endIndexA - k + 1);
        break;
      }
      if (k > endIndexA) {
        System.arraycopy(numbers, j, toReturn, i, endIndexB - j + 1);
        break;
      }
    }
    System.arraycopy(toReturn, 0, numbers, startIndexA, toReturn.length);
  }

  /**
   * @return true if the given index is inside the array range between 0 and
   *         array.length (exclusive).
   */
  public static boolean isValidIndex(int[] array, int index) {
    return index >= 0 && index < array.length;
  }

  /**
   * @return true if the given index is inside the array range between 0 and
   *         array.length (exclusive).
   */
  public static boolean isValidIndex(double[] array, int index) {
    return index >= 0 && index < array.length;
  }

  /**
   * @return true if the given index is inside the array range between 0 and
   *         array.length (exclusive).
   */
  public static boolean isValidIndex(float[] array, int index) {
    return index >= 0 && index < array.length;
  }

  /**
   * @return true if the given index is inside the array range between 0 and
   *         array.length (exclusive).
   */
  public static boolean isValidIndex(long[] array, int index) {
    return index >= 0 && index < array.length;
  }

  /**
   * @return true if the given index is inside the array range between 0 and
   *         array.length (exclusive).
   */
  public static boolean isValidIndex(boolean[] array, int index) {
    return index >= 0 && index < array.length;
  }

  /**
   * @return true if the given index is inside the array range between 0 and
   *         array.length (exclusive).
   */
  public static boolean isValidIndex(byte[] array, int index) {
    return index >= 0 && index < array.length;
  }

  /**
   * @return true if the given index is inside the array range between 0 and
   *         array.length (exclusive).
   */
  public static <T> boolean isValidIndex(T[] array, int index) {
    return index >= 0 && index < array.length;
  }
}
