package de.jungblut.math;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import de.jungblut.util.Tuple;

public final class DenseDoubleMatrix {

  public static final double NOT_FLAGGED = 0.0d;

  protected final double[][] matrix;
  protected final int numRows;
  protected final int numColumns;

  public DenseDoubleMatrix(int rows, int columns) {
    this.numRows = rows;
    this.numColumns = columns;
    this.matrix = new double[rows][columns];
  }

  public DenseDoubleMatrix(int rows, int columns, double defaultValue) {
    this.numRows = rows;
    this.numColumns = columns;
    this.matrix = new double[rows][columns];

    for (int i = 0; i < numRows; i++) {
      Arrays.fill(matrix[i], defaultValue);
    }
  }

  public DenseDoubleMatrix(int rows, int columns, Random rand) {
    this.numRows = rows;
    this.numColumns = columns;
    this.matrix = new double[rows][columns];

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        matrix[i][j] = rand.nextDouble();
      }
    }
  }

  public DenseDoubleMatrix(double[][] otherMatrix) {
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
  public final double get(int row, int col) {
    return this.matrix[row][col];
  }

  /**
   * Get a whole column of the matrix as integer array.
   * 
   * @param col
   * @return
   */
  public final double[] getColumn(int col) {
    final double[] column = new double[numRows];
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
  public final DenseDoubleVector getColumnVector(int col) {
    return new DenseDoubleVector(getColumn(col));
  }

  /**
   * Get the matrix as 2-dimensional integer array (first index is the row,
   * second the column) to faster access the values.
   * 
   * @return
   */
  public final double[][] getValues() {
    return matrix;
  }

  /**
   * Get a single row of the matrix as an integer array.
   * 
   * @param row
   * @return
   */
  public final double[] getRow(int row) {
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
  public final DenseDoubleVector getRowVector(int row) {
    return new DenseDoubleVector(getRow(row));
  }

  public final void set(int row, int col, double value) {
    this.matrix[row][col] = value;
  }

  /**
   * Returns the size of the matrix as string (ROWSxCOLUMNS).
   * 
   * @return
   */
  public String sizeToString() {
    return numRows + "x" + numColumns;
  }

  public final Tuple<DenseDoubleMatrix, DenseDoubleVector> splitLastColumn() {
    DenseDoubleMatrix m = new DenseDoubleMatrix(getRowCount(),
        getColumnCount() - 1);
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount() - 1; j++) {
        m.set(i, j, get(i, j));
      }
    }
    DenseDoubleVector v = new DenseDoubleVector(getColumn(getColumnCount() - 1));
    return new Tuple<DenseDoubleMatrix, DenseDoubleVector>(m, v);
  }

  /**
   * Creates two matrices out of this by the given percentage. It uses a random
   * function to determine which rows should belong to the matrix including the
   * given percentage amount of rows.
   * 
   * @param percentage A float value between 0.0f and 1.0f
   * @return A tuple which includes two matrices, the first contains the
   *         percentage of the rows from the original matrix (rows are chosen
   *         randomly) and the second one contains all other rows.
   */
  public final Tuple<DenseDoubleMatrix, DenseDoubleMatrix> splitRandomMatrices(
      float percentage) {
    if (percentage < 0.0f || percentage > 1.0f) {
      throw new IllegalArgumentException(
          "Percentage must be between 0.0 and 1.0! Given " + percentage);
    }

    if (percentage == 1.0f) {
      return new Tuple<DenseDoubleMatrix, DenseDoubleMatrix>(this, null);
    } else if (percentage == 0.0f) {
      return new Tuple<DenseDoubleMatrix, DenseDoubleMatrix>(null, this);
    }

    final Random rand = new Random(System.nanoTime());
    int firstMatrixRowsCount = Math.round(percentage * numRows);

    // we first choose needed rows number of items to pick
    final HashSet<Integer> lowerMatrixRowIndices = new HashSet<Integer>();
    int missingRows = firstMatrixRowsCount;
    while (missingRows > 0) {
      final int nextIndex = rand.nextInt(numRows);
      if (lowerMatrixRowIndices.add(nextIndex)) {
        missingRows--;
      }
    }

    // make to new matrixes
    final double[][] firstMatrix = new double[firstMatrixRowsCount][numColumns];
    int firstMatrixIndex = 0;
    final double[][] secondMatrix = new double[numRows - firstMatrixRowsCount][numColumns];
    int secondMatrixIndex = 0;

    // then we loop over all items and put split the matrix
    for (int r = 0; r < numRows; r++) {
      if (lowerMatrixRowIndices.contains(r)) {
        firstMatrix[firstMatrixIndex++] = matrix[r];
      } else {
        secondMatrix[secondMatrixIndex++] = matrix[r];
      }
    }

    return new Tuple<DenseDoubleMatrix, DenseDoubleMatrix>(
        new DenseDoubleMatrix(firstMatrix), new DenseDoubleMatrix(secondMatrix));
  }

  public final DenseBooleanMatrix getNonDefaultBooleanMatrix() {
    DenseBooleanMatrix m = new DenseBooleanMatrix(this.numRows, this.numColumns);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        m.set(i, j, this.matrix[i][j] != NOT_FLAGGED);
      }
    }
    return m;
  }

  @Override
  public String toString() {
    return Arrays.deepToString(matrix);
  }

}
