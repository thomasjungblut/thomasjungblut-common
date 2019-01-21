package de.jungblut.writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.WritableComparable;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.SingleEntryDoubleVector;
import de.jungblut.math.named.NamedDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

/**
 * New and updated VectorWritable class that has all the other fancy
 * combinations of vectors that are possible in my math library.<br/>
 * This class is not compatible to the one in the clustering package that has a
 * totally different byte alignment in binary files.
 * 
 * @author thomas.jungblut
 * 
 */
public final class VectorWritable implements WritableComparable<VectorWritable> {

  // TODO if we make them distinct bits, we can add the named vector as an
  // additional bit instead of an additional byte
  private static final byte DENSE = 0;
  private static final byte SPARSE = 1;
  private static final byte SINGLE = 2;

  private DoubleVector vector;

  public VectorWritable() {
    super();
  }

  public VectorWritable(VectorWritable v) {
    this.vector = v.getVector();
  }

  public VectorWritable(DoubleVector v) {
    this.vector = v;
  }

  @Override
  public final void write(DataOutput out) throws IOException {
    writeVector(this.vector, out);
  }

  @Override
  public final void readFields(DataInput in) throws IOException {
    this.vector = readVector(in);
  }

  @Override
  public final int compareTo(VectorWritable o) {
    return compareVector(this, o);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((vector == null) ? 0 : vector.hashCode());
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
    VectorWritable other = (VectorWritable) obj;
    if (vector == null) {
      if (other.vector != null)
        return false;
    } else if (!vector.equals(other.vector))
      return false;
    return true;
  }

  /**
   * @return the embedded vector
   */
  public DoubleVector getVector() {
    return vector;
  }

  @Override
  public String toString() {
    return vector.toString();
  }

  public static void writeVector(DoubleVector vector, DataOutput out)
      throws IOException {

    if (vector.isSparse()) {
      out.writeByte(SPARSE);
      out.writeInt(vector.getLength());
      out.writeInt(vector.getDimension());
      Iterator<DoubleVectorElement> iterateNonZero = vector.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        out.writeInt(next.getIndex());
        out.writeDouble(next.getValue());
      }
    } else if (vector.isSingle()) {
      // single vectors are also dense, thus we will put it before the dense
      // condition in order to save 4 bytes for the length encoding
      out.writeByte(SINGLE);
      out.writeDouble(vector.get(0));
    } else if (!vector.isSparse()) {
      out.writeByte(DENSE);
      out.writeInt(vector.getLength());
      for (int i = 0; i < vector.getDimension(); i++) {
        out.writeDouble(vector.get(i));
      }
    } else {
      throw new IllegalArgumentException("Can't serialize vector of type: "
          + vector.getClass());
    }

    if (vector.isNamed() && vector.getName() != null) {
      out.writeBoolean(true);
      out.writeUTF(vector.getName());
    } else {
      out.writeBoolean(false);
    }
  }

  public static DoubleVector readVector(DataInput in) throws IOException {

    int typeByte = in.readByte();
    DoubleVector vector = null;
    switch (typeByte) {
      case SPARSE:
        int length = in.readInt();
        int dim = in.readInt();
        vector = new SparseDoubleVector(dim);
        for (int i = 0; i < length; i++) {
          int index = in.readInt();
          double value = in.readDouble();
          vector.set(index, value);
        }
        break;
      case DENSE:
        length = in.readInt();
        vector = new DenseDoubleVector(length);
        for (int i = 0; i < length; i++) {
          vector.set(i, in.readDouble());
        }
        break;
      case SINGLE:
        vector = new SingleEntryDoubleVector(in.readDouble());
        break;
      default:
        throw new IllegalArgumentException(
            "Can't deserialize vector of type byte: " + typeByte);
    }

    if (in.readBoolean()) {
      vector = new NamedDoubleVector(in.readUTF(), vector);
    }
    return vector;
  }

  public static int compareVector(VectorWritable a, VectorWritable o) {
    return compareVector(a.getVector(), o.getVector());
  }

  public static int compareVector(DoubleVector a, DoubleVector o) {
    DoubleVector subtract = a.subtract(o);
    return (int) subtract.sum();
  }

  public static VectorWritable wrap(DoubleVector a) {
    return new VectorWritable(a);
  }
}
