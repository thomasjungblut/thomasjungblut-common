package de.jungblut.datastructure;

import java.util.Arrays;

import com.google.common.base.Preconditions;

public class Permutations<T extends Comparable<? super T>> {

  private final T[] array;
  private boolean firstCall = true;

  public Permutations(T[] array) {
    this.array = Preconditions.checkNotNull(array, "array");
    Arrays.sort(array);
  }

  public T[] nextPermutation() {
    if (firstCall) {
      firstCall = false;
      return array;
    }

    int k = -1;
    for (int i = 0; i < array.length - 1; i++) {
      if (array[i].compareTo(array[i + 1]) < 0) {
        k = i;
      }
    }

    if (k >= 0) {
      int j = -1;
      for (int i = 0; i < array.length; i++) {
        if (array[k].compareTo(array[i]) < 0) {
          j = i;
        }
      }

      ArrayUtils.swap(array, k, j);

      reverse(k + 1);

      return array;
    }

    return end();
  }

  private void reverse(int start) {
    int idx = 0;
    int end = (array.length + start) / 2;
    for (int i = start; i < end; i++) {
      ArrayUtils.swap(array, i, array.length - (++idx));
    }
  }

  private T[] end() {
    return null;
  }

}
