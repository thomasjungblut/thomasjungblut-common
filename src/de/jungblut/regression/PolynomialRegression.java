package de.jungblut.regression;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.normalize.Normalizer;
import de.jungblut.math.tuple.Tuple3;

/**
 * Polynomial Regression.
 * 
 * @author thomas.jungblut
 * 
 */
public final class PolynomialRegression {

  private final DoubleMatrix x;
  private final DoubleVector y;
  private final double lambda;
  private final DoubleVector mean;
  private final DoubleVector stddev;
  private final boolean normalize;

  private DoubleVector theta;

  /**
   * Creates a new regression.
   * 
   * @param x the training input.
   * @param y the outcome of the trainingset.
   * @param lambda the regularization parameter.
   * @param normalize true if trainingset should be mean center normalized.
   */
  public PolynomialRegression(DoubleMatrix x, DoubleVector y, double lambda,
      boolean normalize) {
    super();
    this.normalize = normalize;
    if (normalize) {
      Tuple3<DoubleMatrix, DoubleVector, DoubleVector> featureNormalize = Normalizer
          .featureNormalize(x);
      this.x = featureNormalize.getFirst();
      this.mean = featureNormalize.getSecond();
      this.stddev = featureNormalize.getThird();
    } else {
      this.x = x;
      this.mean = null;
      this.stddev = null;
    }
    this.y = y;
    this.lambda = lambda;
  }

  /**
   * Trains the regression model with the {@link Fmincg} optimizer.
   * 
   * @param numIterations the number of iterations to make.
   * @param verbose output the progress to STDOUT if true.
   * @return the learned theta parameters.
   */
  public DoubleVector trainModel(int numIterations, boolean verbose) {
    RegressionCostFunction f = new RegressionCostFunction(x, y, lambda);
    DoubleVector initialTheta = new DenseDoubleVector(x.getColumnCount() + 1,
        1.0d);
    theta = Fmincg.minimizeFunction(f, initialTheta, numIterations, verbose);
    return theta;
  }

  /**
   * Predicts the output by the given input.
   * 
   * @return the predicted vector.
   */
  public DoubleVector predict(DenseDoubleMatrix input) {
    DenseDoubleMatrix in = input;
    if (normalize) {
      in = (DenseDoubleMatrix) in.subtract(mean).divide(stddev);
    }
    return new DenseDoubleMatrix(DenseDoubleVector.ones(in.getRowCount()), in)
        .multiplyVector(theta);
  }

  /**
   * Calculates the mean squared error.
   * 
   * @param prediction the prediction to check against the given real outcome.
   * @return the error
   */
  public double meanSquaredError(DoubleVector prediction) {
    return (y.subtract(prediction).pow(2).sum() / y.getLength());
  }

  /*
   * Some useful accessors for internal state
   */

  public DoubleVector getTheta() {
    return theta;
  }

  public void setTheta(DenseDoubleVector theta) {
    this.theta = theta;
  }

  public DoubleMatrix getX() {
    return x;
  }

  public DoubleVector getY() {
    return y;
  }

  public double getLambda() {
    return lambda;
  }

  public DoubleVector getMean() {
    return mean;
  }

  public DoubleVector getStddev() {
    return stddev;
  }

  public boolean isNormalized() {
    return normalize;
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
