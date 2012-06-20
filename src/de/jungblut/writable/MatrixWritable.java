package de.jungblut.writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.dense.DenseDoubleMatrix;

/**
 * Majorly designed for dense matrices, can be extended for sparse ones as well.
 * 
 */
public final class MatrixWritable implements Writable {

  private DoubleMatrix mat;

  public MatrixWritable() {
  }

  public MatrixWritable(DoubleMatrix mat) {
    this.mat = mat;

  }

  @Override
  public void readFields(DataInput in) throws IOException {
    mat = read(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    write(mat, out);
  }

  public static void write(DoubleMatrix mat, DataOutput out) throws IOException {
    out.writeInt(mat.getRowCount());
    out.writeInt(mat.getColumnCount());
    for (int row = 0; row < mat.getRowCount(); row++) {
      for (int col = 0; col < mat.getColumnCount(); col++) {
        out.writeDouble(mat.get(row, col));
      }
    }
  }

  public static DoubleMatrix read(DataInput in) throws IOException {
    DoubleMatrix mat = new DenseDoubleMatrix(in.readInt(), in.readInt());
    for (int row = 0; row < mat.getRowCount(); row++) {
      for (int col = 0; col < mat.getColumnCount(); col++) {
        mat.set(row, col, in.readDouble());
      }
    }
    return mat;
  }

}
