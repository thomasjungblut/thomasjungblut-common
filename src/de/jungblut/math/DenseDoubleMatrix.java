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

  public DenseDoubleMatrix(DenseDoubleVector first,
      DenseDoubleMatrix otherMatrix) {
    this(otherMatrix.getRowCount(), otherMatrix.getColumnCount() + 1);
    setColumn(0, first.toArray());
    for (int col = 1; col < otherMatrix.getColumnCount() + 1; col++)
      setColumn(col, otherMatrix.getColumn(col-1));
  }

  public DenseDoubleMatrix(double[] v, int rows, int columns) {
    this.matrix = new double[rows][columns];

    for (int i = 0; i < rows; i++) {
      System.arraycopy(v, i * columns, this.matrix[i], 0, columns);
    }

    int index = 0;
    for (int col = 0; col < columns; col++) {
      for (int row = 0; row < rows; row++) {
        matrix[row][col] = v[index++];
      }
    }

    this.numRows = rows;
    this.numColumns = columns;
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

  public final void setRow(int row, double[] value) {
    this.matrix[row] = value;
  }

  public final void setColumn(int col, double[] values) {
    for (int i = 0; i < getRowCount(); i++) {
      this.matrix[i][col] = values[i];
    }
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

  public final DenseDoubleMatrix multiply(double scalar) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(this.numRows, this.numColumns);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        m.set(i, j, this.matrix[i][j] * scalar);
      }
    }
    return m;
  }

  public final DenseDoubleMatrix multiply(DenseDoubleMatrix other) {
    // if (JCUDAMatrixUtils.CUDA_AVAILABLE) {
    // return JCUDAMatrixUtils.multiply(this, other);
    // } else {
    if (this.numColumns != other.getRowCount()) {
      throw new IllegalArgumentException(
          "multiply: nonconformant arguments (this is " + this.numRows + "x"
              + this.numColumns + ", other is " + other.getRowCount() + "x"
              + other.getColumnCount() + ")");
    }

    DenseDoubleMatrix matrix = new DenseDoubleMatrix(this.numRows,
        other.numColumns);

    final int m = this.numRows;
    final int n = this.numColumns;
    final int p = other.numColumns;

    for (int j = p; --j >= 0;) {
      for (int i = m; --i >= 0;) {
        double s = 0;
        for (int k = n; --k >= 0;) {
          s += get(i, k) * other.get(k, j);
        }
        matrix.set(i, j, s + matrix.get(i, j));
      }
    }

    return matrix;
    // }
  }

  /**
   * Multiplies this matrix per element with a binary matrix.
   */
  public final DenseDoubleMatrix multiplyElementWise(DenseBooleanMatrix other) {
    DenseDoubleMatrix matrix = new DenseDoubleMatrix(this.numRows,
        this.numColumns);

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        matrix.set(i, j, this.get(i, j) * (other.get(i, j) ? 1.0d : 0.0d));
      }
    }

    return matrix;
  }

  public final DenseDoubleVector multiplyVector(DenseDoubleVector v) {
    DenseDoubleVector vector = new DenseDoubleVector(this.getRowCount());
    for (int row = 0; row < numRows; row++) {
      double sum = 0.0d;
      for (int col = 0; col < numColumns; col++) {
        sum += (matrix[row][col] * v.get(col));
      }
      vector.set(row, sum);
    }

    return vector;
  }

  public DenseDoubleMatrix transpose() {
    DenseDoubleMatrix m = new DenseDoubleMatrix(this.numColumns, this.numRows);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        m.set(j, i, this.matrix[i][j]);
      }
    }
    return m;
  }

  /**
   * = (amount - m)
   */
  public DenseDoubleMatrix subtractBy(double amount) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(this.numRows, this.numColumns);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        m.set(i, j, amount - this.matrix[i][j]);
      }
    }
    return m;
  }

  /**
   * = (m - amount)
   */
  public DenseDoubleMatrix subtract(double amount) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(this.numRows, this.numColumns);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        m.set(i, j, this.matrix[i][j] - amount);
      }
    }
    return m;
  }

  /**
   * this-other
   */
  public DenseDoubleMatrix subtract(DenseDoubleMatrix other) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(this.numRows, this.numColumns);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        m.set(i, j, this.matrix[i][j] - other.get(i, j));
      }
    }
    return m;
  }

  /**
   * this+other
   */
  public DenseDoubleMatrix add(DenseDoubleMatrix other) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(this.numRows, this.numColumns);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        m.set(i, j, this.matrix[i][j] + other.get(i, j));
      }
    }
    return m;
  }

  public DenseDoubleMatrix pow(int x) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(this.numRows, this.numColumns);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        // for lower order polynomials it is faster to loop
        double value = 0.0d;
        if (x < 5) {
          for (int f = 1; f < x; f++)
            value += matrix[i][j] * matrix[i][j];
        } else {
          value = Math.pow(matrix[i][j], x);
        }

        m.set(i, j, value);
      }
    }
    return m;
  }

  public DenseDoubleMatrix slice(int rows, int cols) {
    return slice(0, rows, 0, cols);
  }

  public DenseDoubleMatrix slice(int rowOffset, int rowMax, int colOffset,
      int colMax) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(rowMax - rowOffset, colMax
        - colOffset);
    for (int row = rowOffset; row < rowMax; row++) {
      for (int col = colOffset; col < colMax; col++) {
        m.set(row - rowOffset, col - colOffset, this.get(row, col));
      }
    }
    return m;
  }

  public double sumElements() {
    double x = 0.0d;
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++) {
        x += Math.abs(matrix[i][j]);
      }
    }
    return x;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(matrix);
    result = prime * result + numColumns;
    result = prime * result + numRows;
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
    DenseDoubleMatrix other = (DenseDoubleMatrix) obj;
    if (!Arrays.deepEquals(matrix, other.matrix))
      return false;
    if (numColumns != other.numColumns)
      return false;
    if (numRows != other.numRows)
      return false;
    return true;
  }

  @Override
  public String toString() {
    if (numRows < 10) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < numRows; i++) {
        sb.append(Arrays.toString(matrix[i]));
        sb.append('\n');
      }
      return sb.toString();
    } else {
      return numRows + "x" + numColumns;
    }
  }

  public static DenseDoubleMatrix eye(int dimension) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(dimension, dimension);

    for (int i = 0; i < dimension; i++) {
      m.set(i, i, 1);
    }

    return m;
  }

  public static DenseDoubleMatrix copy(DenseDoubleMatrix matrix) {
    final double[][] src = matrix.getValues();
    final double[][] dest = new double[matrix.getRowCount()][matrix
        .getColumnCount()];

    for (int i = 0; i < dest.length; i++)
      System.arraycopy(src[i], 0, dest[i], 0, src[i].length);

    return new DenseDoubleMatrix(dest);
  }

  // this is actually strange, but works like this in octave
  public static DenseDoubleMatrix multiplyTransposedVectors(
      DenseDoubleVector transposed, DenseDoubleVector normal) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(transposed.getLength(),
        normal.getLength());
    for (int row = 0; row < transposed.getLength(); row++) {
      for (int col = 0; col < normal.getLength(); col++) {
        m.set(row, col, transposed.get(row) * normal.get(col));
      }
    }

    return m;
  }

  public static double error(DenseDoubleMatrix a, DenseDoubleMatrix b) {
    return a.subtract(b).sumElements();
  }

}
