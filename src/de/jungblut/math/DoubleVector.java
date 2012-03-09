package de.jungblut.math;

import java.util.Iterator;

public interface DoubleVector {

  public double get(int index);

  public int getLength();

  public int getDimension();

  public void set(int index, double value);

  public DoubleVector add(DoubleVector v);

  public DoubleVector add(double scalar);

  public DoubleVector subtract(DoubleVector v);

  public DoubleVector subtract(double v);

  public DoubleVector multiply(double scalar);

  public DoubleVector multiply(DoubleVector vector);

  /**
   * = vector/scalar
   */
  public DoubleVector divide(double scalar);

  public DoubleVector pow(int x);

  public DoubleVector sqrt();

  public double sum();

  /**
   * = scalar/vector
   */
  public DoubleVector divideFrom(double scalar);

  public double dot(DoubleVector s);

  public DoubleVector slice(int length);

  public DoubleVector slice(int offset, int length);

  public double max();

  public double min();

  public double[] toArray();

  public DoubleVector deepCopy();

  public Iterator<DoubleVectorElement> iterateNonZero();

  public Iterator<DoubleVectorElement> iterate();

  public boolean isSparse();

  public static final class DoubleVectorElement {

    private int index;
    private double value;

    public DoubleVectorElement() {
      super();
    }

    public DoubleVectorElement(int index, double value) {
      super();
      this.index = index;
      this.value = value;
    }

    public final int getIndex() {
      return index;
    }

    public final double getValue() {
      return value;
    }

    public final void setIndex(int in) {
      this.index = in;
    }

    public final void setValue(double in) {
      this.value = in;
    }
  }

}
