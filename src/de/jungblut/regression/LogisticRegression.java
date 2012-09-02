package de.jungblut.regression;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Minimizer;

/**
 * Logistic regression.
 * 
 * @author thomas.jungblut
 * 
 */
public class LogisticRegression {

  private final DoubleMatrix x;
  private final DoubleVector y;
  private final double lambda;

  private DoubleVector theta;

  /**
   * Creates a new logistic regression.
   * 
   * @param x the training input.
   * @param y the binary outcome (0 or 1).
   * @param lambda the regularization parameter.
   */
  public LogisticRegression(DoubleMatrix x, DoubleVector y, double lambda) {
    super();
    this.x = x;
    this.y = y;
    this.lambda = lambda;
  }

  /**
   * Trains the logistic regression model with the given optimizer.
   * 
   * @param minimizer the minimizer to use to train this model.
   * @param numIterations the number of iterations to make.
   * @param verbose output the progress to STDOUT if true.
   * @return the learned theta parameters.
   */
  public DoubleVector trainModel(Minimizer minimizer, int numIterations,
      boolean verbose) {
    LogisticRegressionCostFunction fnc = new LogisticRegressionCostFunction(x,
        y, lambda);
    DoubleVector initialTheta = new DenseDoubleVector(x.getColumnCount() + 1,
        1.0d);
    theta = minimizer.minimize(fnc, initialTheta, numIterations, verbose);
    return theta;
  }

  /**
   * Predicts the output by the given input. Everything greater than the given
   * threshold will classified as 1 whereas anything lower than the threshold
   * will be 0.
   * 
   * @return the predicted vector consisting out of zeroes and ones.
   */
  public DoubleVector predict(DenseDoubleMatrix input, double threshold) {
    DoubleVector vec = new DenseDoubleMatrix(DenseDoubleVector.ones(input
        .getRowCount()), input).multiplyVector(theta);
    for (int i = 0; i < vec.getLength(); i++) {
      vec.set(i, vec.get(i) > threshold ? 1.0d : 0.0d);
    }
    return vec;
  }

}
