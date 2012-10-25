package de.jungblut.math.cuda;

import de.jungblut.math.DoubleMatrix;

/**
 * Helper class and data holder for matrices and their operations.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MatrixDimension {

  // dimensions
  private final int m;
  private final int n;
  private final int k;

  // leading dimensions
  private final int ldA;
  private final int ldB;
  private final int ldC;

  // transpose?
  private final boolean transposeA;
  private final boolean transposeB;

  /**
   * Creates matrix dimensions from two matrices. Transpose behaviour is that
   * nothing will be transposed.
   */
  public MatrixDimension(DoubleMatrix a, DoubleMatrix b) {
    this(a, b, false, false);
  }

  /**
   * Creates matrix dimensions from two matrices.
   * 
   * @param a matrix A
   * @param b matrix B
   * @param transposeA true if transpose A
   * @param transposeB true if tranpose B
   */
  public MatrixDimension(DoubleMatrix a, DoubleMatrix b, boolean transposeA,
      boolean transposeB) {
    this.transposeA = transposeA;
    this.transposeB = transposeB;
    int m = a.getRowCount();
    int n = b.getColumnCount();
    int k = a.getColumnCount();

    // leading dimensions
    int ldA = a.getRowCount();
    int ldB = b.getRowCount();
    int ldC = a.getRowCount();

    // recalculate the parameters for transposes
    if (transposeA && transposeB) {
      m = a.getColumnCount();
      n = b.getRowCount();
      k = b.getColumnCount();
      ldC = a.getColumnCount();
    } else if (transposeB) {
      n = b.getRowCount();
    } else if (transposeA) {
      m = a.getColumnCount();
      k = a.getRowCount();
      ldC = a.getColumnCount();
    }

    this.m = m;
    this.n = n;
    this.k = k;
    this.ldA = ldA;
    this.ldB = ldB;
    this.ldC = ldC;
  }

  public int getM() {
    return m;
  }

  public int getN() {
    return n;
  }

  public int getK() {
    return k;
  }

  public int getLdA() {
    return ldA;
  }

  public int getLdB() {
    return ldB;
  }

  public int getLdC() {
    return ldC;
  }

  public boolean isTransposeA() {
    return transposeA;
  }

  public boolean isTransposeB() {
    return transposeB;
  }

}
