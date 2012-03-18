package de.jungblut.math.normalize;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.util.Tuple;
import de.jungblut.util.Tuple3;

public class Normalizer {

  public static Tuple<DoubleMatrix, DoubleVector> meanNormalizeRows(
      DoubleMatrix matrix) {

    DoubleVector meanVector = new DenseDoubleVector(matrix.getRowCount());

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

    return new Tuple<DoubleMatrix, DoubleVector>(matrix, meanVector);
  }

  /**
   * @return the normalized (0 mean and stddev of 1) as well as the mean and the
   *         stddev.
   */
  public static Tuple3<DoubleMatrix, DoubleVector, DoubleVector> featureNormalize(
      DoubleMatrix x) {
    DenseDoubleMatrix toReturn = new DenseDoubleMatrix(x.getRowCount(),
        x.getColumnCount());
    DoubleVector meanVector = new DenseDoubleVector(x.getColumnCount());
    DoubleVector stddevVector = new DenseDoubleVector(x.getColumnCount());

    for (int col = 0; col < x.getColumnCount(); col++) {
      DoubleVector column = x.getColumnVector(col);
      double mean = column.sum() / column.getLength();
      meanVector.set(col, mean);
      double var = column.subtract(mean).pow(2).sum() * 1
          / (column.getLength() - 1);
      stddevVector.set(col, Math.sqrt(var));
    }

    for (int col = 0; col < x.getColumnCount(); col++) {
      DoubleVector column = x.getColumnVector(col)
          .subtract(meanVector.get(col)).divide(stddevVector.get(col));
      toReturn.setColumn(col, column.toArray());
    }

    return new Tuple3<DoubleMatrix, DoubleVector, DoubleVector>(toReturn,
        meanVector, stddevVector);
  }

}
