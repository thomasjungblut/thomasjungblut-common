package de.jungblut.math;

import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Preconditions;

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

    return new Tuple3<>(toReturn, meanVector, stddevVector);
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
      double add = f.evaluateCost(tmp).getCost();
      tmp.set(i, vector.get(i) - stepSize);
      double diff = f.evaluateCost(tmp).getCost();
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
   * @return a log'd matrix that was guarded against edge cases of the
   *         logarithm.
   */
  public static DoubleVector logVector(DoubleVector input) {
    DenseDoubleVector log = new DenseDoubleVector(input.getDimension());
    for (int col = 0; col < log.getDimension(); col++) {
      log.set(col, guardLogarithm(input.get(col)));
    }
    return log;
  }

  /**
   * Scales a matrix into the interval given by min and max.
   * 
   * @param input the input value.
   * @param fromMin the lower bound of the input interval.
   * @param fromMax the upper bound of the input interval.
   * @param toMin the lower bound of the target interval.
   * @param toMax the upper bound of the target interval.
   * @return the new matrix with scaled values.
   */
  public static DoubleMatrix minMaxScale(DoubleMatrix input, double fromMin,
      double fromMax, double toMin, double toMax) {
    DoubleMatrix newOne = new DenseDoubleMatrix(input.getRowCount(),
        input.getColumnCount());
    double[][] array = input.toArray();
    for (int row = 0; row < newOne.getRowCount(); row++) {
      for (int col = 0; col < newOne.getColumnCount(); col++) {
        newOne.set(row, col,
            minMaxScale(array[row][col], fromMin, fromMax, toMin, toMax));
      }
    }
    return newOne;
  }

  /**
   * Scales a vector into the interval given by min and max.
   * 
   * @param input the input vector.
   * @param fromMin the lower bound of the input interval.
   * @param fromMax the upper bound of the input interval.
   * @param toMin the lower bound of the target interval.
   * @param toMax the upper bound of the target interval.
   * @return the new vector with scaled values.
   */
  public static DoubleVector minMaxScale(DoubleVector input, double fromMin,
      double fromMax, double toMin, double toMax) {
    DoubleVector newOne = new DenseDoubleVector(input.getDimension());
    double[] array = input.toArray();
    for (int i = 0; i < array.length; i++) {
      newOne.set(i, minMaxScale(array[i], fromMin, fromMax, toMin, toMax));
    }
    return newOne;
  }

  /**
   * Scales a single input into the interval given by min and max.
   * 
   * @param x the input value.
   * @param fromMin the lower bound of the input interval.
   * @param fromMax the upper bound of the input interval.
   * @param toMin the lower bound of the target interval.
   * @param toMax the upper bound of the target interval.
   * @return the bounded value.
   */
  public static double minMaxScale(double x, double fromMin, double fromMax,
      double toMin, double toMax) {
    return (x - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin;
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

  /**
   * This is actually taken from Kaggle's C# implementation: {@link https
   * ://www.kaggle.com/c/SemiSupervisedFeatureLearning
   * /forums/t/919/auc-implementation/6136#post6136}.
   * 
   * @param outcomePredictedPairs the list of PredictionOutcomePair: class (0 or
   *          1) -> predicted value
   * @return the AUC value.
   */
  public static double computeAUC(
      List<PredictionOutcomePair> outcomePredictedPairs) {

    // order by the predicted value
    Collections.sort(outcomePredictedPairs);

    int n = outcomePredictedPairs.size();
    int numOnes = 0;
    for (PredictionOutcomePair tuple : outcomePredictedPairs) {
      if (tuple.getOutcomeClass() == 1) {
        numOnes++;
      }
    }

    if (numOnes == 0 || numOnes == n) {
      return 1d;
    }

    long tp0, tn;
    long truePos = tp0 = numOnes;
    long accum = tn = 0;
    double threshold = outcomePredictedPairs.get(0).getPrediction();
    for (int i = 0; i < n; i++) {
      double actualValue = outcomePredictedPairs.get(i).getOutcomeClass();
      double predictedValue = outcomePredictedPairs.get(i).getPrediction();
      if (predictedValue != threshold) { // threshold changes
        threshold = predictedValue;
        accum += tn * (truePos + tp0); // 2* the area of trapezoid
        tp0 = truePos;
        tn = 0;
      }
      tn += 1 - actualValue; // x-distance between adjacent points
      truePos -= actualValue;
    }
    accum += tn * (truePos + tp0); // 2 * the area of trapezoid
    return (double) accum / (2 * numOnes * (n - numOnes));
  }

  public static class PredictionOutcomePair implements
      Comparable<PredictionOutcomePair> {

    private final int outcomeClass;
    private final double prediction;

    private PredictionOutcomePair(int outcomeClass, double prediction) {
      this.outcomeClass = outcomeClass;
      this.prediction = prediction;
    }

    public static PredictionOutcomePair from(int outcomeClass, double prediction) {
      Preconditions.checkArgument(outcomeClass == 0 || outcomeClass == 1,
          "Outcome class must be 0 or 1! Supplied: " + outcomeClass);
      return new PredictionOutcomePair(outcomeClass, prediction);
    }

    @Override
    public int compareTo(PredictionOutcomePair o) {
      return Double.compare(prediction, o.prediction);
    }

    public int getOutcomeClass() {
      return this.outcomeClass;
    }

    public double getPrediction() {
      return this.prediction;
    }

  }

}
