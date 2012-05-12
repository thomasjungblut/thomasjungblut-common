package de.jungblut.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.WritableComparable;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

/**
 * Writable and comparable wrapper for sparse and dense vectors. <br/>
 * TODO this must be moved out of the clustering model section
 * 
 * @author thomas.jungblut
 * 
 */
public final class VectorWritable implements WritableComparable<VectorWritable> {

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

  /*
   * Some oldschool compatibility convenience constructors
   */

  public VectorWritable(double x) {
    this.vector = new DenseDoubleVector(new double[] { x });
  }

  public VectorWritable(double x, double y) {
    this.vector = new DenseDoubleVector(new double[] { x, y });
  }

  public VectorWritable(double[] arr) {
    this.vector = new SparseDoubleVector(arr);
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
   * @return the vector
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
    out.writeBoolean(vector.isSparse());
    out.writeInt(vector.getLength());
    if (vector.isSparse()) {
      out.writeInt(vector.getDimension());
      Iterator<DoubleVectorElement> iterateNonZero = vector.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        out.writeInt(next.getIndex());
        out.writeDouble(next.getValue());
      }
    } else {
      for (int i = 0; i < vector.getDimension(); i++) {
        out.writeDouble(vector.get(i));
      }
    }
  }

  public static DoubleVector readVector(DataInput in) throws IOException {
    boolean sparse = in.readBoolean();
    int length = in.readInt();
    DoubleVector vector = null;
    if (sparse) {
      int dim = in.readInt();
      vector = new SparseDoubleVector(dim);
      for (int i = 0; i < length; i++) {
        int index = in.readInt();
        double value = in.readDouble();
        vector.set(index, value);
      }
    } else {
      vector = new DenseDoubleVector(length);
      for (int i = 0; i < length; i++) {
        vector.set(i, in.readDouble());
      }
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
