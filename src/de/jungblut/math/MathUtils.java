package de.jungblut.math;

import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.math.tuple.Tuple3;

/**
 * Math utils that features normalizations and other fancy stuff.
 * 
 * @author thomas.jungblut
 * 
 */
public class MathUtils {

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

  /**
   * Creates a new matrix consisting out of polynomials of the input matrix.<br/>
   * Considering you want to do a 2 polynomial out of 3 columns you get:<br/>
   * (SEED: x^1 | y^1 | z^1 )| x^2 | y^2 | z^2 for the columns of the returned
   * matrix.
   * 
   * @param seed matrix to add polynoms of it.
   * @param num how many polynoms, 2 for quadratic, 3 for cubic and so forth.
   * @return the new matrix.
   */
  public static DenseDoubleMatrix createPolynomials(DenseDoubleMatrix seed,
      int num) {
    if (num == 1)
      return seed;
    DenseDoubleMatrix m = new DenseDoubleMatrix(seed.getRowCount(),
        seed.getColumnCount() * num);
    int index = 0;
    for (int c = 0; c < m.getColumnCount(); c += num) {
      double[] column = seed.getColumn(index++);
      m.setColumn(c, column);
      for (int i = 2; i < num + 1; i++) {
        DoubleVector pow = new DenseDoubleVector(column).pow(i);
        m.setColumn(c + i - 1, pow.toArray());
      }
    }
    return m;
  }

}
