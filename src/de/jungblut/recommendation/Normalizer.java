package de.jungblut.recommendation;

import de.jungblut.math.DenseDoubleMatrix;

public class Normalizer {

  public static DenseDoubleMatrix meanNormalize(DenseDoubleMatrix matrix) {

    for (int row = 0; row < matrix.getRowCount(); row++) {
      double mean = 0.0d;
      int nonZeroElements = 0;
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        double val = matrix.get(row, column);
        if (val != 0.0d) {
          mean += val;
          nonZeroElements++;
        }
      }
      if (nonZeroElements != 0.0d)
        mean = mean / nonZeroElements;
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        matrix.set(row, column, matrix.get(row, column) - mean);
      }
    }

    return matrix;
  }

}
