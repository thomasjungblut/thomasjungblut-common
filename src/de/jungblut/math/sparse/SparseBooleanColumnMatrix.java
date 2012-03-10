package de.jungblut.math.sparse;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Iterator;

import de.jungblut.math.BooleanMatrix;
import de.jungblut.math.BooleanVector;
import de.jungblut.math.BooleanVector.BooleanVectorElement;

public final class SparseBooleanColumnMatrix implements BooleanMatrix {

  protected final TIntObjectHashMap<SparseBooleanVector> matrix = new TIntObjectHashMap<>();
  protected final int numRows;
  protected final int numColumns;

  public SparseBooleanColumnMatrix(int rows, int columns) {
    this.numRows = rows;
    this.numColumns = columns;
  }

  public SparseBooleanColumnMatrix(BooleanMatrix other) {
    this(other.getRowCount(), other.getColumnCount());
    for (int col : other.columnIndices()) {
      matrix.put(col, new SparseBooleanVector(other.getColumnVector(col)));
    }
  }

  /**
   * Get a specific value of the matrix.
   * 
   * @param row
   * @param col
   * @return Returns the integer value at in the column at the row.
   */
  @Override
  public final boolean get(int row, int col) {
    SparseBooleanVector sparseBooleanVector = this.matrix.get(col);
    if (sparseBooleanVector != null)
      return sparseBooleanVector.get(row);
    else
      return false;
  }

  /**
   * Returns the number of columns in the matrix.
   * 
   * @return
   */
  @Override
  public final int getColumnCount() {
    return numColumns;
  }

  @Override
  public final BooleanVector getColumnVector(int col) {
    return new SparseBooleanVector(matrix.get(col));
  }

  /**
   * Returns the number of rows in this matrix.
   * 
   * @return
   */
  @Override
  public final int getRowCount() {
    return numRows;
  }

  /**
   * Get a single row of the matrix as a vector.
   * 
   * @param row
   * @return
   */
  @Override
  public final BooleanVector getRowVector(int row) {
    SparseBooleanVector vec = new SparseBooleanVector(getColumnCount());
    for (int col : columnIndices()) {
      vec.set(col, matrix.get(col).get(row));
    }
    return vec;
  }

  @Override
  public final void set(int row, int col, boolean value) {
    SparseBooleanVector sparseBooleanVector = matrix.get(col);
    if (sparseBooleanVector == null) {
      sparseBooleanVector = new SparseBooleanVector(getRowCount());
      matrix.put(col, sparseBooleanVector);
    }
    sparseBooleanVector.set(row, value);
  }

  @Override
  public BooleanMatrix transpose() {
    SparseBooleanColumnMatrix m = new SparseBooleanColumnMatrix(
        this.numColumns, this.numRows);
    for (int col : this.matrix.keys()) {
      Iterator<BooleanVectorElement> iterateNonZero = matrix.get(col)
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        BooleanVectorElement e = iterateNonZero.next();
        m.set(col, e.getIndex(), e.getValue());
      }
    }
    return m;
  }

  /**
   * Returns the size of the matrix as string (ROWSxCOLUMNS).
   * 
   * @return
   */
  public String sizeToString() {
    return numRows + "x" + numColumns;
  }

  @Override
  public String toString() {
    return sizeToString();
  }

  @Override
  public int[] columnIndices() {
    return matrix.keys();
  }

}
