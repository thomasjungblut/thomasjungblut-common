package de.jungblut.math.normalize;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.math.tuple.Tuple3;

public class Normalizer {

  /**
   * @return normalized matrix (0 mean and stddev of 1) as well as the mean.
   */
  public static Tuple<DoubleMatrix, DoubleVector> meanNormalizeRows(
      DoubleMatrix matrix) {

    DoubleVector meanVector = new DenseDoubleVector(matrix.getRowCount());

    for (int row = 0; row < matrix.getRowCount(); row++) {
      double mean = 0.0d;
      int nonZeroElements = 0;
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        double val = matrix.get(row, column);
        if (val != DoubleMatrix.NOT_FLAGGED) {
          mean += val;
          nonZeroElements++;
        }
      }
      if (nonZeroElements != 0.0d)
        mean = mean / nonZeroElements;
      meanVector.set(row, mean);
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        double val = matrix.get(row, column);
        if (val != DoubleMatrix.NOT_FLAGGED) {
          matrix.set(row, column, val - mean);
        }
      }
    }

    return new Tuple<>(matrix, meanVector);
  }

  /**
   * @return the normalized matrix (0 mean and stddev of 1) as well as the mean
   *         and the stddev.
   */
  public static Tuple3<DoubleMatrix, DoubleVector, DoubleVector> featureNormalize(
      DoubleMatrix x) {
    return featureNormalize(x, true);
  }

  /**
   * @return the normalized matrix (0 mean and stddev of 1) as well as the mean
   *         and the stddev.
   */
  public static Tuple3<DoubleMatrix, DoubleVector, DoubleVector> featureNormalize(
      DoubleMatrix x, boolean normalizeLastColumn) {
    DenseDoubleMatrix toReturn = new DenseDoubleMatrix(x.getRowCount(),
        x.getColumnCount());
    int length = x.getColumnCount();
    if (!normalizeLastColumn) {
      length = length - 1;
    }
    DoubleVector meanVector = new DenseDoubleVector(length);
    DoubleVector stddevVector = new DenseDoubleVector(length);
    for (int col = 0; col < length; col++) {
      DoubleVector column = x.getColumnVector(col);
      double mean = column.sum() / column.getLength();
      meanVector.set(col, mean);
      double var = column.subtract(mean).pow(2).sum() / column.getLength();
      stddevVector.set(col, Math.sqrt(var));
    }

    for (int col = 0; col < length; col++) {
      DoubleVector column = x.getColumnVector(col)
          .subtract(meanVector.get(col)).divide(stddevVector.get(col));
      toReturn.setColumn(col, column.toArray());
    }

    return new Tuple3<DoubleMatrix, DoubleVector, DoubleVector>(toReturn,
        meanVector, stddevVector);
  }

}
