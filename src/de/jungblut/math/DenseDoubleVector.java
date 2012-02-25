package de.jungblut.math;

import java.util.Arrays;

public final class DenseDoubleVector {

  private final double[] vector;

  public DenseDoubleVector(double[] arr) {
    // normally, we should make a deep copy
    this.vector = arr;
  }

  public final double get(int index) {
    return vector[index];
  }

  public final int getLength() {
    return vector.length;
  }

  final void set(int index, double value) {
    vector[index] = value;
  }

  /*
   * MATH stuff
   */

  public final void add(DenseDoubleVector v) {
    for (int i = 0; i < v.getLength(); i++) {
      this.set(i, this.get(i) + v.get(i));
    }
  }

  public final double[] toArray() {
    return vector;
  }

  @Override
  public final String toString() {
    return Arrays.toString(vector);
  }

}
