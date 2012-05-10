package de.jungblut.clustering;

import java.util.Iterator;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.function.DoubleDoubleVectorFunction;
import de.jungblut.math.function.DoubleVectorFunction;

public class IdentifiableDoubleVector implements DoubleVector {

  private int id;
  private DoubleVector nested;

  public IdentifiableDoubleVector(int id, DoubleVector nested) {
    this.id = id;
    this.nested = nested;
  }

  public double get(int index) {
    return nested.get(index);
  }

  public int getLength() {
    return nested.getLength();
  }

  public int getDimension() {
    return nested.getDimension();
  }

  public void set(int index, double value) {
    nested.set(index, value);
  }

  public DoubleVector apply(DoubleVectorFunction func) {
    return nested.apply(func);
  }

  public DoubleVector apply(DoubleVector other, DoubleDoubleVectorFunction func) {
    return nested.apply(other, func);
  }

  public DoubleVector add(DoubleVector v) {
    return nested.add(v);
  }

  public DoubleVector add(double scalar) {
    return nested.add(scalar);
  }

  public DoubleVector subtract(DoubleVector v) {
    return nested.subtract(v);
  }

  public DoubleVector subtract(double scalar) {
    return nested.subtract(scalar);
  }

  public DoubleVector multiply(double scalar) {
    return nested.multiply(scalar);
  }

  public DoubleVector multiply(DoubleVector vector) {
    return nested.multiply(vector);
  }

  public DoubleVector divide(double scalar) {
    return nested.divide(scalar);
  }

  public DoubleVector divideFrom(double scalar) {
    return nested.divideFrom(scalar);
  }

  public DoubleVector pow(int x) {
    return nested.pow(x);
  }

  public DoubleVector abs() {
    return nested.abs();
  }

  public DoubleVector sqrt() {
    return nested.sqrt();
  }

  public double sum() {
    return nested.sum();
  }

  public double dot(DoubleVector s) {
    return nested.dot(s);
  }

  public DoubleVector slice(int length) {
    return nested.slice(length);
  }

  public DoubleVector slice(int offset, int length) {
    return nested.slice(offset, length);
  }

  public double max() {
    return nested.max();
  }

  public double min() {
    return nested.min();
  }

  public double[] toArray() {
    return nested.toArray();
  }

  public DoubleVector deepCopy() {
    return new IdentifiableDoubleVector(id, nested.deepCopy());
  }

  public Iterator<DoubleVectorElement> iterateNonZero() {
    return nested.iterateNonZero();
  }

  public Iterator<DoubleVectorElement> iterate() {
    return nested.iterate();
  }

  public boolean isSparse() {
    return nested.isSparse();
  }

  public int getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((nested == null) ? 0 : nested.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IdentifiableDoubleVector other = (IdentifiableDoubleVector) obj;
    if (nested == null) {
      if (other.nested != null)
        return false;
    } else if (!nested.equals(other.nested))
      return false;
    return true;
  }
  
  

}
