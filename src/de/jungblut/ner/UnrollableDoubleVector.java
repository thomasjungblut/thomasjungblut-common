package de.jungblut.ner;

import java.util.Iterator;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.function.DoubleDoubleVectorFunction;
import de.jungblut.math.function.DoubleVectorFunction;

/**
 * Unrollable proxy double vector class, that wraps multiple vectors into one
 * that can be later unrolled.
 * 
 * @author thomas.jungblut
 * 
 */
public final class UnrollableDoubleVector implements DoubleVector {

  private final DoubleVector mainVector;
  private final DoubleVector[] sideVectors;

  public UnrollableDoubleVector(DoubleVector mainVector,
      DoubleVector[] sideVectors) {
    super();
    this.mainVector = mainVector;
    this.sideVectors = sideVectors;
  }

  public DoubleVector getMainVector() {
    return this.mainVector;
  }

  public DoubleVector[] getSideVectors() {
    return this.sideVectors;
  }

  @Override
  public double get(int index) {
    return this.mainVector.get(index);
  }

  @Override
  public int getLength() {
    return this.mainVector.getLength();
  }

  @Override
  public int getDimension() {
    return this.mainVector.getDimension();
  }

  @Override
  public void set(int index, double value) {
    this.mainVector.set(index, value);
  }

  @Override
  public DoubleVector apply(DoubleVectorFunction func) {
    return this.mainVector.apply(func);
  }

  @Override
  public DoubleVector apply(DoubleVector other, DoubleDoubleVectorFunction func) {
    return this.mainVector.apply(other, func);
  }

  @Override
  public DoubleVector add(DoubleVector v) {
    return this.mainVector.add(v);
  }

  @Override
  public DoubleVector add(double scalar) {
    return this.mainVector.add(scalar);
  }

  @Override
  public DoubleVector subtract(DoubleVector v) {
    return this.mainVector.subtract(v);
  }

  @Override
  public DoubleVector subtract(double scalar) {
    return this.mainVector.subtract(scalar);
  }

  @Override
  public DoubleVector subtractFrom(double scalar) {
    return this.mainVector.subtractFrom(scalar);
  }

  @Override
  public DoubleVector multiply(double scalar) {
    return this.mainVector.multiply(scalar);
  }

  @Override
  public DoubleVector multiply(DoubleVector vector) {
    return this.mainVector.multiply(vector);
  }

  @Override
  public DoubleVector divide(double scalar) {
    return this.mainVector.divide(scalar);
  }

  @Override
  public DoubleVector divideFrom(double scalar) {
    return this.mainVector.divideFrom(scalar);
  }

  @Override
  public DoubleVector divideFrom(DoubleVector vector) {
    return this.mainVector.divideFrom(vector);
  }

  @Override
  public DoubleVector divide(DoubleVector vector) {
    return this.mainVector.divide(vector);
  }

  @Override
  public DoubleVector pow(double x) {
    return this.mainVector.pow(x);
  }

  @Override
  public DoubleVector abs() {
    return this.mainVector.abs();
  }

  @Override
  public DoubleVector sqrt() {
    return this.mainVector.sqrt();
  }

  @Override
  public double sum() {
    return this.mainVector.sum();
  }

  @Override
  public double dot(DoubleVector s) {
    return this.mainVector.dot(s);
  }

  @Override
  public DoubleVector slice(int end) {
    return this.mainVector.slice(end);
  }

  @Override
  public DoubleVector slice(int start, int end) {
    return this.mainVector.slice(start, end);
  }

  @Override
  public DoubleVector sliceByLength(int start, int length) {
    return this.mainVector.sliceByLength(start, length);
  }

  @Override
  public double max() {
    return this.mainVector.max();
  }

  @Override
  public double min() {
    return this.mainVector.min();
  }

  @Override
  public int maxIndex() {
    return this.mainVector.maxIndex();
  }

  @Override
  public int minIndex() {
    return this.mainVector.minIndex();
  }

  @Override
  public double[] toArray() {
    return this.mainVector.toArray();
  }

  @Override
  public DoubleVector deepCopy() {
    return this.mainVector.deepCopy();
  }

  @Override
  public Iterator<DoubleVectorElement> iterateNonZero() {
    return this.mainVector.iterateNonZero();
  }

  @Override
  public Iterator<DoubleVectorElement> iterate() {
    return this.mainVector.iterate();
  }

  @Override
  public boolean isSparse() {
    return this.mainVector.isSparse();
  }

  @Override
  public boolean isNamed() {
    return this.mainVector.isNamed();
  }

  @Override
  public String getName() {
    return this.mainVector.getName();
  }

}
