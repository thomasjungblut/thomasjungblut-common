package de.jungblut.math.sparse;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Iterator;

import de.jungblut.math.BooleanMatrix;
import de.jungblut.math.BooleanVector;
import de.jungblut.math.BooleanVector.BooleanVectorElement;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;

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
    SparseDoubleVector sparseDoubleVector = matrix.get(col);
    if (sparseDoubleVector == null)
      return NOT_FLAGGED;
    else
      return sparseDoubleVector.get(row);
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
    // TODO improve for sparse vectors..
    DoubleMatrix result = new SparseDoubleColumnMatrix(this.getRowCount(),
        other.getColumnCount());
    for (int row = 0; row < getRowCount(); row++) {
      for (int col = 0; col < other.getColumnCount(); col++) {
        double sum = 0;
        for (int k = 0; k < getColumnCount(); k++) {
          sum += get(row, k) * other.get(k, col);
        }
        result.set(row, col, sum);
      }
    }
    return result;
  }

  @Override
  public DoubleMatrix slice(int rows, int cols) {
    return slice(0, rows, 0, cols);
  }

  @Override
  public DoubleMatrix slice(int rowOffset, int rowMax, int colOffset, int colMax) {
    DoubleMatrix m = new SparseDoubleColumnMatrix(rowMax - rowOffset, colMax
        - colOffset);
    for (int col : columnIndices()) {
      DoubleVector columnVector = getColumnVector(col);
      columnVector = columnVector.slice(rowOffset, rowMax);
      m.setColumnVector(col - colOffset, columnVector);
    }

    return m;
  }

  @Override
  public DoubleMatrix multiplyElementWise(BooleanMatrix other) {
    SparseDoubleColumnMatrix matrix = new SparseDoubleColumnMatrix(
        this.numRows, this.numColumns);
    for (int col : other.columnIndices()) {
      BooleanVector columnVector = other.getColumnVector(col);
      Iterator<BooleanVectorElement> iterateNonZero = columnVector
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        BooleanVectorElement next = iterateNonZero.next();
        matrix.set(next.getIndex(), col, get(next.getIndex(), col));
      }
    }
    return matrix;
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
    DoubleVector result = new SparseDoubleVector(this.getRowCount());
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      double sum = 0.0d;
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        sum += (e.getValue() * v.get(col));
      }
      result.set(col, sum);
    }
    return result;
  }

  @Override
  public boolean isSparse() {
    return true;
  }

  @Override
  public DoubleMatrix transpose() {
    SparseDoubleColumnMatrix m = new SparseDoubleColumnMatrix(this.numColumns,
        this.numRows);
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        m.set(col, e.getIndex(), e.getValue());
      }
    }
    return m;
  }

  @Override
  public BooleanMatrix getNonDefaultBooleanMatrix() {
    BooleanMatrix mat = new SparseBooleanColumnMatrix(getRowCount(),
        getColumnCount());
    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        mat.set(next.getIndex(), col, true);
      }
    }
    return mat;
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
    SparseDoubleColumnMatrix m = new SparseDoubleColumnMatrix(other);

    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        m.set(e.getIndex(), col,
            get(e.getIndex(), col) - other.get(e.getIndex(), col));
      }
    }

    for (int col : other.columnIndices()) {
      Iterator<DoubleVectorElement> iterateNonZero = other.getColumnVector(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        m.set(e.getIndex(), col,
            get(e.getIndex(), col) - other.get(e.getIndex(), col));
      }
    }

    return m;
  }

  @Override
  public DoubleMatrix subtract(DoubleVector vec) {
    DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    for (int col : this.matrix.keys()) {
      SparseDoubleVector colVec = matrix.get(col);
      result.setColumnVector(col, colVec.subtract(vec.get(col)));
    }
    return result;
  }

  @Override
  public DoubleMatrix divide(DoubleVector vec) {
    DoubleMatrix result = new SparseDoubleColumnMatrix(this);
    for (int col : this.matrix.keys()) {
      SparseDoubleVector colVec = matrix.get(col);
      result.setColumnVector(col, colVec.divide(vec.get(col)));
    }
    return result;
  }

  @Override
  public DoubleMatrix divide(DoubleMatrix other) {
    SparseDoubleColumnMatrix m = new SparseDoubleColumnMatrix(other);

    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        m.set(e.getIndex(), col,
            get(e.getIndex(), col) / other.get(e.getIndex(), col));
      }
    }

    for (int col : other.columnIndices()) {
      Iterator<DoubleVectorElement> iterateNonZero = other.getColumnVector(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        m.set(e.getIndex(), col,
            get(e.getIndex(), col) / other.get(e.getIndex(), col));
      }
    }

    return m;
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
    SparseDoubleColumnMatrix m = new SparseDoubleColumnMatrix(other);

    for (int col : this.matrix.keys()) {
      Iterator<DoubleVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        m.set(e.getIndex(), col,
            get(e.getIndex(), col) + other.get(e.getIndex(), col));
      }
    }

    for (int col : other.columnIndices()) {
      Iterator<DoubleVectorElement> iterateNonZero = other.getColumnVector(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement e = iterateNonZero.next();
        m.set(e.getIndex(), col,
            get(e.getIndex(), col) + other.get(e.getIndex(), col));
      }
    }

    return m;
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
  public double sum() {
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

  @Override
  public int[] columnIndices() {
    return matrix.keys();
  }

}
