package de.jungblut.classification.regression;

import de.jungblut.classification.AbstractClassifier;
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
public final class LogisticRegression extends AbstractClassifier {

  private final double lambda;
  private final Minimizer minimizer;
  private final int numIterations;
  private final boolean verbose;
  private final double threshold;

  // learned weights
  private DoubleVector theta;

  /**
   * Creates a new logistic regression.
   * 
   * @param lambda the regularization parameter.
   * @param minimizer the minimizer to use to train this model.
   * @param numIterations the number of iterations to make.
   * @param threshold the prediction threshold, e.G. 0.5 everything below 0.5
   *          will be predicted as zero, anything above(>) as 1.
   * @param verbose output the progress to STDOUT if true.
   */
  public LogisticRegression(double lambda, Minimizer minimizer,
      int numIterations, double threshold, boolean verbose) {
    super();
    this.lambda = lambda;
    this.minimizer = minimizer;
    this.numIterations = numIterations;
    this.threshold = threshold;
    this.verbose = verbose;
  }

  /**
   * Creates a new logistic regression.
   */
  public LogisticRegression(DoubleVector theta) {
    this(0d, null, 1, 0d, false);
    this.theta = theta;
  }

  @Override
  public void train(DoubleVector[] features, DenseDoubleVector[] outcome) {
    DenseDoubleMatrix x = new DenseDoubleMatrix(features);
    DenseDoubleVector y = new DenseDoubleVector(outcome.length);
    for (int i = 0; i < outcome.length; i++) {
      y.set(i, outcome[i].get(0));
    }
    train(x, y);
  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    DoubleVector biasedFeatures = new DenseDoubleVector(
        features.getLength() + 1, 1d);
    for (int i = 0; i < features.getLength(); i++) {
      biasedFeatures.set(i + 1, features.get(i));
    }
    return new DenseDoubleVector(
        new double[] { LogisticRegressionCostFunction.sigmoid(biasedFeatures
            .dot(theta)) > threshold ? 1.0d : 0.0d });
  }

  @Override
  public int getPredictedClass(DoubleVector features) {
    return (int) predict(features).get(0);
  }

  public void train(DenseDoubleMatrix x, DenseDoubleVector y) {
    LogisticRegressionCostFunction fnc = new LogisticRegressionCostFunction(x,
        y, lambda);
    DoubleVector initialTheta = new DenseDoubleVector(x.getColumnCount() + 1);
    for (int i = 0; i < initialTheta.getLength(); i++) {
      initialTheta.set(i, Math.random());
    }
    theta = minimizer.minimize(fnc, initialTheta, numIterations, verbose);
  }

  /**
   * Predicts the output by the given input.
   * 
   * @return the predicted vector consisting out of zeroes and ones.
   */
  public DoubleVector predict(DenseDoubleMatrix input) {
    DoubleVector vec = new DenseDoubleMatrix(DenseDoubleVector.ones(input
        .getRowCount()), input).multiplyVector(theta);
    for (int i = 0; i < vec.getLength(); i++) {
      vec.set(i,
          LogisticRegressionCostFunction.sigmoid(vec.get(i)) > threshold ? 1.0d
              : 0.0d);
    }
    return vec;
  }

  /**
   * @return the learned weights.FSO
   */
  public DoubleVector getTheta() {
    return theta;
  }
}
