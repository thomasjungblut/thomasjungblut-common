package de.jungblut.math;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.math.tuple.Tuple3;

/**
 * Math utils that features normalizations and other fancy stuff.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MathUtils {

  public static final double EPS = Math.sqrt(2.2E-16);

  private MathUtils() {
    throw new IllegalAccessError();
  }

  /**
   * @return mean normalized matrix (0 mean and stddev of 1) as well as the
   *         mean.
   */
  public static Tuple<DoubleMatrix, DoubleVector> meanNormalizeRows(
      DoubleMatrix pMatrix) {
    DoubleMatrix matrix = new DenseDoubleMatrix(pMatrix.getRowCount(),
        pMatrix.getColumnCount());
    DoubleVector meanVector = new DenseDoubleVector(matrix.getRowCount());
    for (int row = 0; row < matrix.getRowCount(); row++) {
      double mean = 0.0d;
      int nonZeroElements = 0;
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        double val = pMatrix.get(row, column);
        if (val != DoubleMatrix.NOT_FLAGGED) {
          mean += val;
          nonZeroElements++;
        }
      }
      // prevent division by zero
      if (nonZeroElements != 0.0d) {
        mean = mean / nonZeroElements;
      }
      meanVector.set(row, mean);
      for (int column = 0; column < matrix.getColumnCount(); column++) {
        double val = pMatrix.get(row, column);
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
  public static Tuple3<DoubleMatrix, DoubleVector, DoubleVector> meanNormalizeColumns(
      DoubleMatrix x) {
    DenseDoubleMatrix toReturn = new DenseDoubleMatrix(x.getRowCount(),
        x.getColumnCount());
    final int length = x.getColumnCount();
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

  /**
   * Calculates the numerical gradient from a cost function using the central
   * difference theorem. f'(x) = (f(x + h) - f(x - h)) / 2.
   * 
   * @param vector the parameters to derive.
   * @param f the costfunction to return the cost at a given parameterset.
   * @return a numerical gradient.
   */
  public static DoubleVector numericalGradient(DoubleVector vector,
      CostFunction f) {
    DoubleVector gradient = new DenseDoubleVector(vector.getLength());
    DoubleVector tmp = vector.deepCopy();
    for (int i = 0; i < vector.getLength(); i++) {
      double stepSize = EPS * (Math.abs(vector.get(i)) + 1d);
      tmp.set(i, vector.get(i) + stepSize);
      double add = f.evaluateCost(tmp).getFirst().doubleValue();
      tmp.set(i, vector.get(i) - stepSize);
      double diff = f.evaluateCost(tmp).getFirst().doubleValue();
      gradient.set(i, (add - diff) / (2d * stepSize));
    }
    return gradient;
  }

  /**
   * @return a log'd matrix that was guarded against edge cases of the
   *         logarithm.
   */
  public static DoubleMatrix logMatrix(DoubleMatrix input) {
    DenseDoubleMatrix log = new DenseDoubleMatrix(input.getRowCount(),
        input.getColumnCount());
    for (int row = 0; row < log.getRowCount(); row++) {
      for (int col = 0; col < log.getColumnCount(); col++) {
        double d = input.get(row, col);
        log.set(row, col, guardLogarithm(d));
      }
    }
    return log;
  }

  /**
   * @return a log'd value of the input that is guarded.
   */
  public static double guardLogarithm(double input) {
    if (Double.isNaN(input) || Double.isInfinite(input)) {
      return 0d;
    } else if (input <= 0d || input <= -0d) {
      // assume a quite low value of log(1e-5) ~= -11.51
      return -10d;
    } else {
      return FastMath.log(input);
    }
  }

}
