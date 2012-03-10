package de.jungblut.math;


public interface BooleanMatrix {

  public static final double NOT_FLAGGED = 0;

  /**
   * Get a specific value of the matrix.
   * 
   * @param row
   * @param col
   * @return Returns the integer value at in the column at the row.
   */
  public boolean get(int row, int col);

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
  public BooleanVector getColumnVector(int col);

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
  public BooleanVector getRowVector(int row);

  public void set(int row, int col, boolean value);

  public BooleanMatrix transpose();
  
  public int[] columnIndices();

}
