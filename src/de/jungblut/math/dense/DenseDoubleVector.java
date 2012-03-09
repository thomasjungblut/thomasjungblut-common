package de.jungblut.math.dense;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.AbstractIterator;

import de.jungblut.math.DoubleVector;
import de.jungblut.util.Tuple;

public final class DenseDoubleVector implements DoubleVector {

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

  @Override
  public final double get(int index) {
    return vector[index];
  }

  @Override
  public final int getLength() {
    return vector.length;
  }

  @Override
  public int getDimension() {
    return getLength();
  }

  @Override
  public final void set(int index, double value) {
    vector[index] = value;
  }

  /*
   * MATH stuff
   */

  @Override
  public final DoubleVector add(DoubleVector v) {
    DenseDoubleVector newv = new DenseDoubleVector(v.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      newv.set(i, this.get(i) + v.get(i));
    }
    return newv;
  }

  @Override
  public final DoubleVector add(double scalar) {
    DoubleVector newv = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < this.getLength(); i++) {
      newv.set(i, this.get(i) + scalar);
    }
    return newv;
  }

  @Override
  public final DoubleVector subtract(DoubleVector v) {
    DoubleVector newv = new DenseDoubleVector(v.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      newv.set(i, this.get(i) - v.get(i));
    }
    return newv;
  }

  @Override
  public final DoubleVector subtract(double v) {
    DenseDoubleVector newv = new DenseDoubleVector(vector.length);
    for (int i = 0; i < vector.length; i++) {
      newv.set(i, vector[i] - v);
    }
    return newv;
  }

  @Override
  public DoubleVector multiply(double scalar) {
    DoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, this.get(i) * scalar);
    }
    return v;
  }

  @Override
  public DoubleVector multiply(DoubleVector vector) {
    DoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, this.get(i) * vector.get(i));
    }
    return v;
  }

  @Override
  public DoubleVector divide(double scalar) {
    DenseDoubleVector v = new DenseDoubleVector(this.getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, this.get(i) / scalar);
    }
    return v;
  }

  @Override
  public DoubleVector pow(int x) {
    DenseDoubleVector v = new DenseDoubleVector(getLength());
    for (int i = 0; i < v.getLength(); i++) {
      double value = 0.0d;
      // it is faster to multiply when we having ^2
      if (x == 2) {
        value = vector[i] * vector[i];
      } else {
        value = Math.pow(vector[i], x);
      }
      v.set(i, value);
    }
    return v;
  }

  @Override
  public DoubleVector sqrt() {
    DoubleVector v = new DenseDoubleVector(getLength());
    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, Math.sqrt(vector[i]));
    }
    return v;
  }

  @Override
  public double sum() {
    double sum = 0.0d;
    for (int i = 0; i < vector.length; i++) {
      sum += vector[i];
    }
    return sum;
  }

  @Override
  public DoubleVector divideFrom(double scalar) {
    DoubleVector v = new DenseDoubleVector(this.getLength());
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

  @Override
  public double dot(DoubleVector s) {
    double dotProduct = 0.0d;
    for (int i = 0; i < getLength(); i++) {
      dotProduct += this.get(i) * s.get(i);
    }
    return dotProduct;
  }

  @Override
  public DoubleVector slice(int length) {
    return slice(0, length);
  }

  @Override
  public DoubleVector slice(int offset, int length) {
    DoubleVector nv = new DenseDoubleVector(length - offset);

    for (int i = offset; i < length - offset; i++) {
      nv.set(i, vector[i]);
    }

    return nv;
  }

  @Override
  public double max() {
    double max = Double.MIN_VALUE;
    for (int i = 0; i < getLength(); i++) {
      double d = vector[i];
      if (d > max) {
        max = d;
      }
    }
    return max;
  }

  @Override
  public double min() {
    double min = Double.MAX_VALUE;
    for (int i = 0; i < getLength(); i++) {
      double d = vector[i];
      if (d < min) {
        min = d;
      }
    }
    return min;
  }

  @Override
  public final double[] toArray() {
    return vector;
  }

  @Override
  public DoubleVector deepCopy() {
    final double[] src = vector;
    final double[] dest = new double[vector.length];
    System.arraycopy(src, 0, dest, 0, vector.length);
    return new DenseDoubleVector(dest);
  }

  @Override
  public Iterator<DoubleVectorElement> iterateNonZero() {
    return new NonZeroIterator();
  }

  @Override
  public Iterator<DoubleVectorElement> iterate() {
    return new DefaultIterator();
  }

  @Override
  public final String toString() {
    if (getLength() < 20) {
      return Arrays.toString(vector);
    } else {
      return getLength() + "x1";
    }
  }

  private final class NonZeroIterator extends
      AbstractIterator<DoubleVectorElement> {

    private final DoubleVectorElement element = new DoubleVectorElement();
    private final double[] array;
    private int currentIndex = 0;

    private NonZeroIterator() {
      this.array = vector;
    }

    @Override
    protected final DoubleVectorElement computeNext() {
      while (array[currentIndex] == 0.0d) {
        currentIndex++;
        if (currentIndex >= array.length)
          return endOfData();
      }
      element.setIndex(currentIndex);
      element.setValue(array[currentIndex]);
      return element;
    }
  }

  private final class DefaultIterator extends
      AbstractIterator<DoubleVectorElement> {

    private final DoubleVectorElement element = new DoubleVectorElement();
    private final double[] array;
    private int currentIndex = 0;

    private DefaultIterator() {
      this.array = vector;
    }

    @Override
    protected final DoubleVectorElement computeNext() {
      if (currentIndex < array.length) {
        element.setIndex(currentIndex);
        element.setValue(array[currentIndex]);
        currentIndex++;
        return element;
      } else {
        return endOfData();
      }
    }

  }

  public static DenseDoubleVector ones(int num) {
    return new DenseDoubleVector(num, 1.0d);
  }

  public static DenseDoubleVector fromUpTo(double from, double to,
      double stepsize) {
    DenseDoubleVector v = new DenseDoubleVector(
        (int) (Math.round(((to - from) / stepsize) + 0.5)));

    for (int i = 0; i < v.getLength(); i++) {
      v.set(i, from + i * stepsize);
    }
    return v;
  }

  public static List<Tuple<Double, Integer>> sort(DoubleVector vector,
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
