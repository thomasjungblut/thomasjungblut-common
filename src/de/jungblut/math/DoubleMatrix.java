package de.jungblut.math;

import de.jungblut.math.dense.DenseBooleanMatrix;

public interface DoubleMatrix {

  public static final double NOT_FLAGGED = 0.0d;

  /**
   * Get a specific value of the matrix.
   * 
   * @param row
   * @param col
   * @return Returns the integer value at in the column at the row.
   */
  public double get(int row, int col);

  /**
   * Returns the number of columns in the matrix.
   * 
   * @return
   */
  public int getColumnCount();

  /**
   * Get a whole column of the matrix as vector. If the specified column doesn't
   * exist a IllegalArgumentException is thrown.
   * 
   * @param col
   * @return
   * @throws IllegalArgumentException
   */
  public DoubleVector getColumnVector(int col);

  /**
   * Returns the number of rows in this matrix.
   * 
   * @return
   */
  public int getRowCount();

  /**
   * Get a single row of the matrix as a vector.
   * 
   * @param row
   * @return
   */
  public DoubleVector getRowVector(int row);

  public void set(int row, int col, double value);

  public void setColumnVector(int col, DoubleVector column);

  public void setRowVector(int rowIndex, DoubleVector row);

  public DoubleMatrix multiply(double scalar);

  public DoubleMatrix multiply(DoubleMatrix other);

  /**
   * Multiplies this matrix per element with a binary matrix.
   */
  public DoubleMatrix multiplyElementWise(DenseBooleanMatrix other);

  /**
   * Multiplies this matrix per element with a real matrix.
   */
  public DoubleMatrix multiplyElementWise(DoubleMatrix other);

  public DoubleVector multiplyVector(DoubleVector v);

  public DoubleMatrix transpose();

  /**
   * = (amount - matrix value)
   */
  public DoubleMatrix subtractBy(double amount);

  /**
   * = (m - amount)
   */
  public DoubleMatrix subtract(double amount);

  /**
   * this-other
   */
  public DoubleMatrix subtract(DoubleMatrix other);

  /**
   * subtracts each element in a column by the related element in vec
   */
  public DoubleMatrix subtract(DoubleVector vec);

  public DoubleMatrix divide(DoubleVector vec);

  /**
   * this / other
   */
  public DoubleMatrix divide(DoubleMatrix other);

  /**
   * this / scalar
   */
  public DoubleMatrix divide(double scalar);

  /**
   * this+other
   */
  public DoubleMatrix add(DoubleMatrix other);

  public DoubleMatrix pow(int x);

  public double max(int column);

  public double min(int column);

  public double sumElements();

}
