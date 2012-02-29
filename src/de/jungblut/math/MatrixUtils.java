package de.jungblut.math;

/**
 * Includes some sum functions.
 * 
 * @author thomas.jungblut
 * 
 */
public class MatrixUtils {

  public static double sum(DenseDoubleMatrix toSum) {
    double totalSum = 0.0d;
    for (int col = 0; col < toSum.getColumnCount(); col++) {
      double colSum = 0.0d;
      for (int row = 0; row < toSum.getRowCount(); row++) {
        colSum += toSum.get(row, col);
      }
      totalSum += colSum;
    }
    return totalSum;
  }

  public static double sum(DenseDoubleVector toSum) {
    double totalSum = 0.0d;
    for (int row = 0; row < toSum.getLength(); row++) {
      totalSum += toSum.get(row);
    }
    return totalSum;
  }

  public static double sumWhenTrue(DenseDoubleMatrix toSum,
      DenseBooleanMatrix blocker) {
    double totalSum = 0.0d;
    for (int col = 0; col < toSum.getColumnCount(); col++) {
      double colSum = 0.0d;
      for (int row = 0; row < toSum.getRowCount(); row++) {
        if (blocker.get(row, col))
          colSum += toSum.get(row, col);
      }
      totalSum += colSum;
    }
    return totalSum;
  }

}
