package de.jungblut.math;

import java.util.Iterator;

public interface BooleanVector {

  public boolean get(int index);

  public int getLength();

  public boolean[] toArray();

  public Iterator<BooleanVectorElement> iterateNonZero();

  public static final class BooleanVectorElement {

    private int index;
    private boolean value;

    public BooleanVectorElement() {
      super();
    }

    public BooleanVectorElement(int index, boolean value) {
      super();
      this.index = index;
      this.value = value;
    }

    public final int getIndex() {
      return index;
    }

    public final boolean getValue() {
      return value;
    }

    public final void setIndex(int in) {
      this.index = in;
    }

    public final void setValue(boolean in) {
      this.value = in;
    }
  }

}
