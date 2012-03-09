package de.jungblut.math.sparse;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Iterator;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseBooleanMatrix;

public class SparseDoubleColumnMatrix implements DoubleMatrix {

  // int -> vector, where int is the column index and vector the corresponding
  // column vector
  private final TIntObjectHashMap<SparseDoubleVector> matrix = new TIntObjectHashMap<>();
  protected final int numRows;
  protected final int numColumns;

  public SparseDoubleColumnMatrix(int rows, int columns) {
    this.numRows = rows;
    this.numColumns = columns;
  }

  public SparseDoubleColumnMatrix(DoubleMatrix mat) {
    this(mat.getRowCount(), mat.getColumnCount());
    for (int i = 0; i < numColumns; i++) {
      setColumnVector(i, mat.getColumnVector(i));
    }
  }

  @Override
  public double get(int row, int col) {
    return matrix.get(col).get(row);
  }

  @Override
  public int getColumnCount() {
    return numColumns;
  }

  @Override
  public DoubleVector getColumnVector(int col) {
    return matrix.get(col);
  }

  @Override
  public int getRowCount() {
    return numRows;
  }

  @Override
  public DoubleVector getRowVector(int row) {
    int[] keys = matrix.keys();
    DoubleVector v = new SparseDoubleVector(getColumnCount());
    for (int key : keys) {
      v.set(key, get(key, row));
    }
    return v;
  }

  @Override
  public void set(int row, int col, double value) {
    SparseDoubleVector sparseDoubleVector = matrix.get(col);
    if (sparseDoubleVector == null) {
      sparseDoubleVector = new SparseDoubleVector(getRowCount());
      matrix.put(col, sparseDoubleVector);
    }
    sparseDoubleVector.set(row, value);
  }

  @Override
  public void setColumnVector(int col, DoubleVector column) {
    matrix.put(col, new SparseDoubleVector(column));
  }

  @Override
  public void setRowVector(int rowIndex, DoubleVector row) {
    Iterator<DoubleVectorElement> iterateNonZero = row.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      set(rowIndex, next.getIndex(), next.getValue());
    }
  }

  @Override
  public DoubleMatrix multiply(double scalar) {
    DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        result.set(e.getIndex(), col, get(e.getIndex(), col) * scalar);
      }
    }
    return result;
  }

  @Override
  public DoubleMatrix multiply(DoubleMatrix other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DoubleMatrix multiplyElementWise(DenseBooleanMatrix other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DoubleMatrix multiplyElementWise(DoubleMatrix other) {
    DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        result.set(e.getIndex(), col,
            get(e.getIndex(), col) * other.get(e.getIndex(), col));
      }
    }
    return result;
  }

  @Override
  public DoubleVector multiplyVector(DoubleVector v) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DoubleMatrix transpose() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DoubleMatrix subtractBy(double amount) {
    DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        result.set(e.getIndex(), col, amount - get(e.getIndex(), col));
      }
    }
    return result;
  }

  @Override
  public DoubleMatrix subtract(double amount) {
    DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        result.set(e.getIndex(), col, get(e.getIndex(), col) - amount);
      }
    }
    return result;
  }

  @Override
  public DoubleMatrix subtract(DoubleMatrix other) {
    // DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    // for (int col : this.matrix.keys()) {
    // Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
    // .iterateNonZero();
    // while (iterateNonZero.hasNext()) {
    // DoubleVectorElement e = iterateNonZero.next();
    // result.set(e.getIndex(), col,
    // get(e.getIndex(), col) - other.get(e.getIndex(), col));
    // }
    // }
    // return result;
    // TODO
    return null;
  }

  @Override
  public DoubleMatrix subtract(DoubleVector vec) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DoubleMatrix divide(DoubleVector vec) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DoubleMatrix divide(DoubleMatrix other) {
    // TODO
    return null;
  }

  @Override
  public DoubleMatrix divide(double scalar) {
    DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        result.set(e.getIndex(), col, get(e.getIndex(), col) / scalar);
      }
    }
    return result;
  }

  @Override
  public DoubleMatrix add(DoubleMatrix other) {
    // TODO
    return null;
  }

  @Override
  public DoubleMatrix pow(int x) {
    DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        if (x != 2) {
          result.set(e.getIndex(), col, Math.pow(get(e.getIndex(), col), x));
        } else {
          double res = get(e.getIndex(), col);
          result.set(e.getIndex(), col, res * res);
        }
      }
    }
    return result;
  }

  @Override
  public double max(int column) {
    return getColumnVector(column).max();
  }

  @Override
  public double min(int column) {
    return getColumnVector(column).min();
  }

  @Override
  public double sumElements() {
    double res = 0.0d;
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        res += e.getValue();
      }
    }
    return res;
  }

}
