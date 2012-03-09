package de.jungblut.math.dense;

import java.util.Arrays;
import java.util.HashSet;

public final class DenseIntVector {

  private final int[] vector;

  public DenseIntVector(int[] arr) {
    // normally, we should make a deep copy
    this.vector = arr;
  }

  public final int get(int index) {
    return vector[index];
  }

  public final int getLength() {
    return vector.length;
  }

  final void set(int index, int value) {
    vector[index] = value;
  }

  public final int getNumberOfDistinctElements() {
    final int length = getLength();
    final HashSet<Integer> set = new HashSet<Integer>(length);
    for (int i = 0; i < length; i++) {
      set.add(get(i));
    }
    return set.size();
  }

  // in our "hack" environment, this fast retrieval can be made because we
  // have
  // elements starting from 0 and then are
  // incremented. -> Experimental!
  public final int getNumberOfDistinctElementsFast() {
    int high = 0;
    final int length = getLength();
    for (int i = 0; i < length; i++) {
      final int j = get(i);
      if (high < j) {
        high = j;
      }
    }
    // +1 because of zero
    return high + 1;
  }

  public int getMaxValue() {
    int maxValue = vector[0];
    for (int i = 0; i < vector.length; i++) {
      if (maxValue < vector[i]) {
        maxValue = vector[i];
      }
    }
    return maxValue;
  }

  /*
   * MATH stuff
   */

  public final void add(DenseIntVector v) {
    for (int i = 0; i < v.getLength(); i++) {
      this.set(i, this.get(i) + v.get(i));
    }
  }

  public final int[] toArray() {
    return vector;
  }

  @Override
  public final String toString() {
    return Arrays.toString(vector);
  }

}
