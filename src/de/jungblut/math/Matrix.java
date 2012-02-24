package de.jungblut.math;

import java.util.Arrays;
import java.util.HashSet;

public final class Matrix {

  protected final int[][] matrix;
  protected final int numRows;
  protected final int numColumns;

  public Matrix(int rows, int columns) {
    this.numRows = rows;
    this.numColumns = columns;
    this.matrix = new int[rows][columns];
  }

  public Matrix(int[][] otherMatrix) {
    this.matrix = otherMatrix;
    this.numRows = otherMatrix.length;
    this.numColumns = matrix[0].length;
  }

  /**
   * Looks for different values in the column of the matrix.
   * 
   * @param col
   * @return Return true if the column has different values, otherwise false.
   */
  public boolean hasColumnDifferentValues(int col) {
    if (col < numColumns || col > 0) {
      int val = matrix[0][col];
      for (int r = 1; r < numRows; r++) {
        if (matrix[r][col] != val) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets the number of distinct integers in a given column.
   * 
   */
  public final HashSet<Integer> getDistinctElements(int column) {
    final int length = getRowCount();
    final HashSet<Integer> set = new HashSet<Integer>(length);
    for (int i = 0; i < length; i++) {
      set.add(get(i, column));
    }
    return set;
  }

  /**
   * Gets the number of distinct integers in a given column.
   * 
   */
  public final int[] getDistinctElementsAsArray(int column) {
    final HashSet<Integer> distinctElements = getDistinctElements(column);
    final int[] set = new int[distinctElements.size()];
    int index = 0;
    for (Integer i : distinctElements) {
      set[index++] = i;
    }
    return set;
  }

  /**
   * Get a specific value of the matrix.
   * 
   * @param row
   * @param col
   * @return Returns the integer value at in the column at the row.
   */
  public final int get(int row, int col) {
    return this.matrix[row][col];
  }

  /**
   * Get a whole column of the matrix as integer array.
   * 
   * @param col
   * @return
   */
  public final int[] getColumn(int col) {
    final int[] column = new int[numRows];
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
  public final DenseIntVector getColumnVector(int col) {
    return new DenseIntVector(getColumn(col));
  }

  /**
   * Get the matrix as 2-dimensional integer array (first index is the row,
   * second the column) to faster access the values.
   * 
   * @return
   */
  public final int[][] getValues() {
    return matrix;
  }

  /**
   * Get a single row of the matrix as an integer array.
   * 
   * @param row
   * @return
   */
  public final int[] getRow(int row) {
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
  public final DenseIntVector getRowVector(int row) {
    return new DenseIntVector(getRow(row));
  }

  /**
   * Get the highest value of column in the matrix.
   * 
   * @param col
   * @return
   */
  public int maxColumnValue(int col) {
    int max = Integer.MIN_VALUE;
    for (int r = 0; r < numRows; r++) {
      if (matrix[r][col] > max) {
        max = matrix[r][col];
      }
    }
    return max;
  }

  /**
   * Calculates the maximum value for each column and return all maxima as an
   * integer value where the index is the column index.
   * 
   * @return
   */
  public int[] maxColumnValues() {
    int[] maxColumnValues = new int[numColumns];
    for (int c = 0; c < numColumns; c++) {
      int max = Integer.MIN_VALUE;
      for (int r = 0; r < numRows; r++) {
        if (matrix[r][c] > max) {
          max = matrix[r][c];
        }
      }
      maxColumnValues[c] = max;
    }
    return maxColumnValues;
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
