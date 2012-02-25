package de.jungblut.math;

import java.util.Arrays;

public final class DenseDoubleVector {

  private final double[] vector;

  public DenseDoubleVector(int length) {
    this.vector = new double[length];
  }

  public DenseDoubleVector(double[] arr) {
    this.vector = arr;
  }

  public DenseDoubleVector(double[] array, double f1) {
    this.vector = new double[array.length + 1];
    System.arraycopy(array, 0, this.vector, 0, array.length);
    this.vector[array.length] = f1;
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

  public final DenseDoubleVector add(DenseDoubleVector v) {
    DenseDoubleVector newv = new DenseDoubleVector(v.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      newv.set(i, this.get(i) + v.get(i));
    }
    return newv;
  }

  public final DenseDoubleVector subtract(DenseDoubleVector v) {
    DenseDoubleVector newv = new DenseDoubleVector(v.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      newv.set(i, this.get(i) - v.get(i));
    }
    return newv;
  }

  public DenseDoubleVector multiply(double scalar) {
    DenseDoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      this.set(i, this.get(i) * scalar);
    }
    return v;
  }

  /**
   * = vector/scalar
   */
  public DenseDoubleVector divide(double scalar) {
    DenseDoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      this.set(i, this.get(i) / scalar);
    }
    return v;
  }

  /**
   * = scalar/vector
   */
  public DenseDoubleVector divideFrom(double scalar) {
    DenseDoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      this.set(i, scalar / this.get(i));
    }
    return v;
  }

  public double dot(DenseDoubleVector s) {
    double dotProduct = 0.0d;
    for (int i = 0; i < getLength(); i++) {
      dotProduct += this.get(i) * s.get(i);
    }
    return dotProduct;
  }

  public final double[] toArray() {
    return vector;
  }

  @Override
  public final String toString() {
    return Arrays.toString(vector);
  }

  public static DenseDoubleVector copy(DenseDoubleVector vector) {
    final double[] src = vector.vector;
    final double[] dest = new double[vector.getLength()];
    System.arraycopy(src, 0, dest, 0, vector.getLength());
    return new DenseDoubleVector(dest);
  }

}
