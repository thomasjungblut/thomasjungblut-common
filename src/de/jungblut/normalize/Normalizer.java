package de.jungblut.normalize;

import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;
import de.jungblut.util.Tuple;
import de.jungblut.util.Tuple3;

public class Normalizer {

  public static Tuple<DenseDoubleMatrix, DenseDoubleVector> meanNormalizeRows(
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

  /**
   * @return the normalized (0 mean and stddev of 1) as well as the mean and the
   *         stddev.
   */
  public static Tuple3<DenseDoubleMatrix, DenseDoubleVector, DenseDoubleVector> featureNormalize(
      DenseDoubleMatrix x) {
    DenseDoubleMatrix toReturn = new DenseDoubleMatrix(x.getRowCount(),
        x.getColumnCount());
    DenseDoubleVector meanVector = new DenseDoubleVector(x.getColumnCount());
    DenseDoubleVector stddevVector = new DenseDoubleVector(x.getColumnCount());

    for (int col = 0; col < x.getColumnCount(); col++) {
      DenseDoubleVector column = x.getColumnVector(col);
      double mean = column.sum() / column.getLength();
      meanVector.set(col, mean);
      double var = column.subtract(mean).pow(2).sum() *  1 / (column.getLength()-1);
      stddevVector.set(col, Math.sqrt(var));
    }

    for (int col = 0; col < x.getColumnCount(); col++) {
      DenseDoubleVector column = x.getColumnVector(col)
          .subtract(meanVector.get(col)).divide(stddevVector.get(col));
      toReturn.setColumn(col, column.toArray());
    }

    return new Tuple3<DenseDoubleMatrix, DenseDoubleVector, DenseDoubleVector>(
        toReturn, meanVector, stddevVector);
  }

}
