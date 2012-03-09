package de.jungblut.math.dense;

import java.util.Arrays;
import java.util.Random;

public final class DenseBooleanMatrix {

  public static final double NOT_FLAGGED = 0;

  protected final boolean[][] matrix;
  protected final int numRows;
  protected final int numColumns;

  public DenseBooleanMatrix(int rows, int columns) {
    this.numRows = rows;
    this.numColumns = columns;
    this.matrix = new boolean[rows][columns];
  }

  public DenseBooleanMatrix(int rows, int columns, boolean defaultValue) {
    this.numRows = rows;
    this.numColumns = columns;
    this.matrix = new boolean[rows][columns];

    for (int i = 0; i < numRows; i++) {
      Arrays.fill(matrix[i], defaultValue);
    }
  }

  public DenseBooleanMatrix(int rows, int columns, Random rand) {
    this.numRows = rows;
    this.numColumns = columns;
    this.matrix = new boolean[rows][columns];

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        matrix[i][j] = rand.nextBoolean();
      }
    }
  }

  public DenseBooleanMatrix(boolean[][] otherMatrix) {
    this.matrix = otherMatrix;
    this.numRows = otherMatrix.length;
    if (matrix.length > 0)
      this.numColumns = matrix[0].length;
    else
      this.numColumns = numRows;
  }

  /**
   * Get a specific value of the matrix.
   * 
   * @param row
   * @param col
   * @return Returns the integer value at in the column at the row.
   */
  public final boolean get(int row, int col) {
    return this.matrix[row][col];
  }

  /**
   * Get a whole column of the matrix as integer array.
   * 
   * @param col
   * @return
   */
  public final boolean[] getColumn(int col) {
    final boolean[] column = new boolean[numRows];
    for (int r = 0; r < numRows; r++) {
      column[r] = matrix[r][col];
    }
    return column;
  }

  /**
   * Returns the number of columns in the matrix.
   * 
   * @return
   */
  public final int getColumnCount() {
    return numColumns;
  }

  /**
   * Get a whole column of the matrix as vector. If the specified column doesn't
   * exist a IllegalArgumentException is thrown.
   * 
   * @param col
   * @return
   * @throws IllegalArgumentException
   */
  public final DenseBooleanVector getColumnVector(int col) {
    return new DenseBooleanVector(getColumn(col));
  }

  /**
   * Get the matrix as 2-dimensional integer array (first index is the row,
   * second the column) to faster access the values.
   * 
   * @return
   */
  public final boolean[][] getValues() {
    return matrix;
  }

  /**
   * Get a single row of the matrix as an integer array.
   * 
   * @param row
   * @return
   */
  public final boolean[] getRow(int row) {
    return matrix[row];
  }

  /**
   * Returns the number of rows in this matrix.
   * 
   * @return
   */
  public final int getRowCount() {
    return numRows;
  }

  /**
   * Get a single row of the matrix as a vector.
   * 
   * @param row
   * @return
   */
  public final DenseBooleanVector getRowVector(int row) {
    return new DenseBooleanVector(getRow(row));
  }

  public final void set(int row, int col, boolean value) {
    this.matrix[row][col] = value;
  }

  public DenseBooleanMatrix transpose() {
    DenseBooleanMatrix m = new DenseBooleanMatrix(this.numColumns, this.numRows);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        m.set(j, i, this.matrix[i][j]);
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
    return Arrays.deepToString(matrix);
  }

}
