package de.jungblut.math.dense;

import java.util.Arrays;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import de.jungblut.math.BooleanVector;

public final class DenseBooleanVector implements BooleanVector {

  private final boolean[] vector;

  public DenseBooleanVector(boolean[] arr) {
    // normally, we should make a deep copy
    this.vector = arr;
  }

  @Override
  public final boolean get(int index) {
    return vector[index];
  }

  @Override
  public final int getLength() {
    return vector.length;
  }

  final void set(int index, boolean value) {
    vector[index] = value;
  }

  @Override
  public final boolean[] toArray() {
    return vector;
  }

  @Override
  public final String toString() {
    return Arrays.toString(vector);
  }

  @Override
  public Iterator<BooleanVectorElement> iterateNonZero() {
    return new NonZeroIterator();
  }

  private final class NonZeroIterator extends
      AbstractIterator<BooleanVectorElement> {

    private final BooleanVectorElement element = new BooleanVectorElement();
    private final boolean[] array;
    private int currentIndex = 0;

    private NonZeroIterator() {
      this.array = vector;
    }

    @Override
    protected final BooleanVectorElement computeNext() {
      while (array[currentIndex] == false) {
        currentIndex++;
        if (currentIndex >= array.length)
          return endOfData();
      }
      element.setIndex(currentIndex);
      element.setValue(array[currentIndex]);
      return element;
    }
  }

}
