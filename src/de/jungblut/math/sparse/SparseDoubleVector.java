package de.jungblut.math.sparse;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import de.jungblut.math.DoubleVector;

public class SparseDoubleVector implements DoubleVector {

  private final TIntDoubleHashMap vector;
  private final int dimension;

  public SparseDoubleVector(int dimension) {
    this.vector = new TIntDoubleHashMap();
    this.dimension = dimension;
  }

  public SparseDoubleVector(DoubleVector v) {
    this(v.getLength());
    Iterator<DoubleVectorElement> iterateNonZero = v.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      vector.put(next.getIndex(), next.getValue());
    }
  }

  public SparseDoubleVector(double[] arr) {
    this(arr.length);
    for (int i = 0; i < arr.length; i++) {
      vector.put(i, arr[i]);
    }
  }

  public SparseDoubleVector(double[] arr, double f1) {
    this(arr.length + 1);
    vector.put(0, f1);
    for (int i = 1; i < arr.length; i++) {
      vector.put(i, arr[i]);
    }
  }

  @Override
  public double get(int index) {
    return vector.get(index);
  }

  @Override
  public int getLength() {
    return vector.size();
  }

  @Override
  public int getDimension() {
    return dimension;
  }

  @Override
  public void set(int index, double value) {
    vector.put(index, value);
  }

  @Override
  public DoubleVector add(DoubleVector other) {
    DoubleVector result = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> iter = other.iterateNonZero();
    while (iter.hasNext()) {
      DoubleVectorElement e = iter.next();
      int index = e.getIndex();
      result.set(index, this.get(index) + e.getValue());
    }
    return result;
  }

  @Override
  public DoubleVector add(double scalar) {
    DoubleVector v = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> it = v.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      set(e.getIndex(), e.getValue() + scalar);
    }
    return v;
  }

  @Override
  public DoubleVector subtract(DoubleVector other) {
    DoubleVector result = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> iter = other.iterateNonZero();
    while (iter.hasNext()) {
      DoubleVectorElement e = iter.next();
      int index = e.getIndex();
      result.set(index, this.get(index) - e.getValue());
    }
    return result;
  }

  @Override
  public DoubleVector subtract(double scalar) {
    DoubleVector v = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> it = v.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      set(e.getIndex(), e.getValue() - scalar);
    }
    return v;
  }

  @Override
  public DoubleVector multiply(double scalar) {
    DoubleVector v = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> it = v.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      set(e.getIndex(), e.getValue() * scalar);
    }
    return v;
  }

  @Override
  public DoubleVector multiply(DoubleVector s) {
    DoubleVector vec = new SparseDoubleVector(s.getDimension());
    DoubleVector smallestVector = s.getLength() > getLength() ? s : this;
    DoubleVector largerVector = smallestVector == this ? s : s;
    Iterator<DoubleVectorElement> it = smallestVector.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement next = it.next();
      double otherValue = largerVector.get(next.getIndex());
      vec.set(next.getIndex(), next.getValue() * otherValue);
    }

    return vec;
  }

  @Override
  public DoubleVector divide(double scalar) {
    DoubleVector v = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> it = v.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      set(e.getIndex(), e.getValue() / scalar);
    }
    return v;
  }

  @Override
  public DoubleVector pow(int x) {
    DoubleVector v = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> it = v.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      double value = 0.0d;
      if (x == 2) {
        value = e.getValue() * e.getValue();
      } else {
        value = Math.pow(e.getValue(), x);
      }
      set(e.getIndex(), value);
    }
    return v;
  }

  @Override
  public DoubleVector sqrt() {
    DoubleVector v = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> it = v.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      set(e.getIndex(), Math.sqrt(e.getValue()));
    }
    return v;
  }

  @Override
  public double sum() {
    double sum = 0.0d;
    Iterator<DoubleVectorElement> it = iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      sum += e.getValue();
    }
    return sum;
  }

  @Override
  public DoubleVector abs() {
    DoubleVector v = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> it = v.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      set(e.getIndex(), Math.abs(e.getValue()));
    }
    return v;
  }

  @Override
  public DoubleVector divideFrom(double scalar) {
    DoubleVector v = new SparseDoubleVector(this);
    Iterator<DoubleVectorElement> it = v.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      set(e.getIndex(), scalar / e.getValue());
    }
    return v;
  }

  @Override
  public double dot(DoubleVector s) {
    double dotProduct = 0.0d;
    DoubleVector smallestVector = s.getLength() > getLength() ? s : this;
    DoubleVector largerVector = smallestVector == this ? s : s;
    Iterator<DoubleVectorElement> it = smallestVector.iterateNonZero();

    while (it.hasNext()) {
      DoubleVectorElement next = it.next();
      double d = largerVector.get(next.getIndex());
      dotProduct += d * next.getValue();
    }

    return dotProduct;
  }

  @Override
  public DoubleVector slice(int length) {
    return slice(0, length);
  }

  @Override
  public boolean isSparse() {
    return true;
  }

  @Override
  public DoubleVector slice(int offset, int length) {
    // TODO this should be ultra slow
    DoubleVector nv = new SparseDoubleVector(this.dimension);
    for (int i = offset; i < length - offset; i++) {
      nv.set(i, get(i));
    }
    return nv;
  }

  @Override
  public double max() {
    double res = Double.MIN_VALUE;
    Iterator<DoubleVectorElement> it = this.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      if (res < e.getValue()) {
        res = e.getValue();
      }
    }
    return res;
  }

  @Override
  public double min() {
    double res = Double.MAX_VALUE;
    Iterator<DoubleVectorElement> it = this.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      if (res > e.getValue()) {
        res = e.getValue();
      }
    }
    return res;
  }

  @Override
  public double[] toArray() {
    double[] d = new double[dimension];
    Iterator<DoubleVectorElement> it = this.iterateNonZero();
    while (it.hasNext()) {
      DoubleVectorElement e = it.next();
      d[e.getIndex()] = e.getValue();
    }
    return d;
  }

  @Override
  public String toString() {
    if (getLength() < 20) {
      return vector.toString();
    } else {
      return getDimension() + "x1";
    }
  }

  @Override
  public DoubleVector deepCopy() {
    return new SparseDoubleVector(this);
  }

  @Override
  public Iterator<DoubleVectorElement> iterateNonZero() {
    return new NonZeroIterator();
  }

  @Override
  public Iterator<DoubleVectorElement> iterate() {
    return new DefaultIterator();
  }

  private final class NonZeroIterator extends
      AbstractIterator<DoubleVectorElement> {

    private final DoubleVectorElement element = new DoubleVectorElement();
    private TIntDoubleIterator iterator;

    public NonZeroIterator() {
      iterator = vector.iterator();
    }

    @Override
    protected final DoubleVectorElement computeNext() {
      if (iterator.hasNext()) {
        iterator.advance();
        element.setIndex(iterator.key());
        element.setValue(iterator.value());
        return element;
      } else {
        return endOfData();
      }
    }

  }

  private final class DefaultIterator extends
      AbstractIterator<DoubleVectorElement> {

    private final DoubleVectorElement element = new DoubleVectorElement();
    private int index = 0;

    @Override
    protected DoubleVectorElement computeNext() {
      if (index < getDimension()) {
        element.setIndex(index);
        element.setValue(get(index));
        index++;
        return element;
      } else {
        return endOfData();
      }
    }

  }

}
