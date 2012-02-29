package de.jungblut.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.jungblut.util.Tuple;

public final class DenseDoubleVector {

  private final double[] vector;

  public DenseDoubleVector(int length) {
    this.vector = new double[length];
  }

  public DenseDoubleVector(int length, double val) {
    this(length);
    Arrays.fill(vector, val);
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

  public final void set(int index, double value) {
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

  public final DenseDoubleVector add(double scalar) {
    DenseDoubleVector newv = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < this.getLength(); i++) {
      newv.set(i, this.get(i) + scalar);
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
  
  public final DenseDoubleVector subtract(double v) {
    DenseDoubleVector newv = new DenseDoubleVector(vector.length);
    for (int i = 0; i < vector.length; i++) {
      newv.set(i, vector[i] - v);
    }
    return newv;
  }

  public DenseDoubleVector multiply(double scalar) {
    DenseDoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, this.get(i) * scalar);
    }
    return v;
  }

  public DenseDoubleVector multiply(DenseDoubleVector vector) {
    DenseDoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, this.get(i) * vector.get(i));
    }
    return v;
  }

  /**
   * = vector/scalar
   */
  public DenseDoubleVector divide(double scalar) {
    DenseDoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, this.get(i) / scalar);
    }
    return v;
  }

  public DenseDoubleVector pow(double power) {
    DenseDoubleVector v = new DenseDoubleVector(getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, Math.pow(vector[i], power));
    }
    return v;
  }
  
  public DenseDoubleVector sqrt() {
    DenseDoubleVector v = new DenseDoubleVector(getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, Math.sqrt(vector[i]));
    }
    return v;
  }

  public double sum() {
    double sum = 0.0d;
    for (int i = 0; i < vector.length; i++) {
      sum += vector[i];
    }
    return sum;
  }

  /**
   * = scalar/vector
   */
  public DenseDoubleVector divideFrom(double scalar) {
    DenseDoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      if (this.get(i) != 0.0d) {
        double result = scalar / this.get(i);
        v.set(i, result);
      } else {
        v.set(i, 0.0d);
      }
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

  public DenseDoubleVector slice(int length) {
    return slice(0, length);
  }

  public DenseDoubleVector slice(int offset, int length) {
    DenseDoubleVector nv = new DenseDoubleVector(length - offset);

    for (int i = offset; i < length - offset; i++) {
      nv.set(i, vector[i]);
    }

    return nv;
  }

  public final double[] toArray() {
    return vector;
  }

  @Override
  public final String toString() {
    return Arrays.toString(vector);
  }

  public static DenseDoubleVector ones(int num) {
    return new DenseDoubleVector(num, 1.0d);
  }

  public static DenseDoubleVector copy(DenseDoubleVector vector) {
    final double[] src = vector.vector;
    final double[] dest = new double[vector.getLength()];
    System.arraycopy(src, 0, dest, 0, vector.getLength());
    return new DenseDoubleVector(dest);
  }

  public static List<Tuple<Double, Integer>> sort(DenseDoubleVector vector,
      final Comparator<Double> scoreComparator) {
    List<Tuple<Double, Integer>> list = new ArrayList<Tuple<Double, Integer>>(
        vector.getLength());
    for (int i = 0; i < vector.getLength(); i++) {
      list.add(new Tuple<Double, Integer>(vector.get(i), i));
    }
    Collections.sort(list, new Comparator<Tuple<Double, Integer>>() {
      @Override
      public int compare(Tuple<Double, Integer> o1, Tuple<Double, Integer> o2) {
        return scoreComparator.compare(o1.getFirst(), o2.getFirst());
      }
    });
    return list;
  }

}
