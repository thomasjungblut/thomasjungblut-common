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

  @Override
  public double get(int index) {
    return nested.get(index);
  }

  @Override
  public int getLength() {
    return nested.getLength();
  }

  @Override
  public int getDimension() {
    return nested.getDimension();
  }

  @Override
  public void set(int index, double value) {
    nested.set(index, value);
  }

  @Override
  public DoubleVector apply(DoubleVectorFunction func) {
    return nested.apply(func);
  }

  @Override
  public DoubleVector apply(DoubleVector other, DoubleDoubleVectorFunction func) {
    return nested.apply(other, func);
  }

  @Override
  public DoubleVector add(DoubleVector v) {
    return nested.add(v);
  }

  @Override
  public DoubleVector add(double scalar) {
    return nested.add(scalar);
  }

  @Override
  public DoubleVector subtract(DoubleVector v) {
    return nested.subtract(v);
  }

  @Override
  public DoubleVector subtract(double scalar) {
    return nested.subtract(scalar);
  }

  @Override
  public DoubleVector multiply(double scalar) {
    return nested.multiply(scalar);
  }

  @Override
  public DoubleVector multiply(DoubleVector vector) {
    return nested.multiply(vector);
  }

  @Override
  public DoubleVector divide(double scalar) {
    return nested.divide(scalar);
  }

  @Override
  public DoubleVector divideFrom(double scalar) {
    return nested.divideFrom(scalar);
  }

  @Override
  public DoubleVector pow(int x) {
    return nested.pow(x);
  }

  @Override
  public DoubleVector abs() {
    return nested.abs();
  }

  @Override
  public DoubleVector sqrt() {
    return nested.sqrt();
  }

  @Override
  public double sum() {
    return nested.sum();
  }

  @Override
  public double dot(DoubleVector s) {
    return nested.dot(s);
  }

  @Override
  public DoubleVector slice(int length) {
    return nested.slice(length);
  }

  @Override
  public DoubleVector slice(int offset, int length) {
    return nested.slice(offset, length);
  }

  @Override
  public double max() {
    return nested.max();
  }

  @Override
  public double min() {
    return nested.min();
  }

  @Override
  public double[] toArray() {
    return nested.toArray();
  }

  @Override
  public DoubleVector deepCopy() {
    return new IdentifiableDoubleVector(id, nested.deepCopy());
  }

  @Override
  public Iterator<DoubleVectorElement> iterateNonZero() {
    return nested.iterateNonZero();
  }

  @Override
  public Iterator<DoubleVectorElement> iterate() {
    return nested.iterate();
  }

  @Override
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
