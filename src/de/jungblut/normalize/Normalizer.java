package de.jungblut.normalize;

import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;
import de.jungblut.util.Tuple;

public class Normalizer {

  public static Tuple<DenseDoubleMatrix, DenseDoubleVector> meanNormalize(
      DenseDoubleMatrix matrix) {

    DenseDoubleVector meanVector = new DenseDoubleVector(matrix.getRowCount());

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
      meanVector.set(row, mean);
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        double val = matrix.get(row, column);
        if (val != DenseDoubleMatrix.NOT_FLAGGED) {
          matrix.set(row, column, val - mean);
        }
      }
    }

    return new Tuple<DenseDoubleMatrix, DenseDoubleVector>(matrix, meanVector);
  }

}
