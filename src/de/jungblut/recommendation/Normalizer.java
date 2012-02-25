package de.jungblut.recommendation;

import de.jungblut.math.DenseDoubleMatrix;

public class Normalizer {

  public static DenseDoubleMatrix meanNormalize(DenseDoubleMatrix matrix) {

    for (int row = 0; row < matrix.getRowCount(); row++) {
      double mean = 0.0d;
      int nonZeroElements = 0;
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        double val = matrix.get(row, column);
        if (val != DenseDoubleMatrix.NOT_FLAGGED) {
          mean += val;
          nonZeroElements++;
        }
      }
      if (nonZeroElements != 0.0d)
        mean = mean / nonZeroElements;
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        double val = matrix.get(row, column);
        if (val != DenseDoubleMatrix.NOT_FLAGGED)
          matrix.set(row, column, val - mean);
      }
    }

    return matrix;
  }

}
