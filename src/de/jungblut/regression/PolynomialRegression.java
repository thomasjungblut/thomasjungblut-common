package de.jungblut.regression;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.MathUtils;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Minimizer;
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
      Tuple3<DoubleMatrix, DoubleVector, DoubleVector> featureNormalize = MathUtils
          .meanNormalizeColumns(x);
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
   * Trains the regression model with the given optimizer.
   * 
   * @param minimizer the minimizer to use to train this model.
   * @param numIterations the number of iterations to make.
   * @param verbose output the progress to STDOUT if true.
   * @return the learned theta parameters.
   */
  public DoubleVector trainModel(Minimizer minimizer, int numIterations,
      boolean verbose) {
    RegressionCostFunction f = new RegressionCostFunction(x, y, lambda);
    DoubleVector initialTheta = new DenseDoubleVector(x.getColumnCount() + 1,
        1.0d);
    theta = minimizer.minimize(f, initialTheta, numIterations, verbose);
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

}
