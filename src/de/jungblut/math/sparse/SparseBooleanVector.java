package de.jungblut.math.sparse;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import de.jungblut.math.BooleanVector;

public final class SparseBooleanVector implements BooleanVector {

  private static final int TRUE = 1;

  private final TIntIntHashMap vector = new TIntIntHashMap();
  private final int length;

  public SparseBooleanVector(int length) {
    this.length = length;
  }

  public SparseBooleanVector(BooleanVector vec) {
    this(vec.getLength());
    Iterator<BooleanVectorElement> iterateNonZero = vec.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      vector.put(iterateNonZero.next().getIndex(), TRUE);
    }
  }

  public SparseBooleanVector(boolean[] arr) {
    this(arr.length);
    for (int i = 0; i < arr.length; i++) {
      if (arr[i])
        vector.put(i, TRUE);
    }
  }

  @Override
  public final boolean get(int index) {
    return vector.get(index) == TRUE;
  }

  @Override
  public final int getLength() {
    return length;
  }

  final void set(int index, boolean value) {
    if (value)
      vector.put(index, TRUE);
  }

  @Override
  public final boolean[] toArray() {
    boolean[] vec = new boolean[length];
    for (int index : vector.keys())
      vec[index] = true;
    return vec;
  }

  @Override
  public Iterator<BooleanVectorElement> iterateNonZero() {
    return new NotFalseIterator();
  }

  private final class NotFalseIterator extends
      AbstractIterator<BooleanVectorElement> {

    private final BooleanVectorElement element = new BooleanVectorElement();
    private final int[] array;
    private int currentIndex = 0;

    private NotFalseIterator() {
      this.array = vector.keys();
    }

    @Override
    protected final BooleanVectorElement computeNext() {
      if (currentIndex < array.length) {
        currentIndex++;
      } else {
        return endOfData();
      }

      element.setIndex(currentIndex);
      element.setValue(vector.get(currentIndex) == TRUE);

      return element;
    }
  }

}
