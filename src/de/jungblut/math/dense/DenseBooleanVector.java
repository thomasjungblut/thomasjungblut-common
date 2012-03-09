package de.jungblut.math.dense;

import java.util.Arrays;

public final class DenseBooleanVector {

  private final boolean[] vector;

  public DenseBooleanVector(boolean[] arr) {
    // normally, we should make a deep copy
    this.vector = arr;
  }

  public final boolean get(int index) {
    return vector[index];
  }

  public final int getLength() {
    return vector.length;
  }

  final void set(int index, boolean value) {
    vector[index] = value;
  }

  public final boolean[] toArray() {
    return vector;
  }

  @Override
  public final String toString() {
    return Arrays.toString(vector);
  }

}
