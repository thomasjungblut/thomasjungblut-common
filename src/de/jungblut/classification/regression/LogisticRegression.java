package de.jungblut.classification.regression;

import static de.jungblut.math.activation.ActivationFunctionSelector.SIGMOID;

import java.util.Random;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.classification.nn.MultilayerPerceptron;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Minimizer;

/**
 * Logistic regression (binary classification). For multiple classes, better use
 * the {@link MultilayerPerceptron} with no hidden layer.
 * 
 * @author thomas.jungblut
 * 
 */
public final class LogisticRegression extends AbstractClassifier {

  public static long SEED = System.currentTimeMillis();

  private final double lambda;
  private final Minimizer minimizer;
  private final int numIterations;
  private final boolean verbose;

  // learned weights
  private DoubleVector theta;

  private Random random;

  /**
   * Creates a new logistic regression.
   * 
   * @param lambda the regularization parameter.
   * @param minimizer the minimizer to use to train this model.
   * @param numIterations the number of iterations to make.
   * @param verbose output the progress to STDOUT if true.
   */
  public LogisticRegression(double lambda, Minimizer minimizer,
      int numIterations, boolean verbose) {
    super();
    this.lambda = lambda;
    this.minimizer = minimizer;
    this.numIterations = numIterations;
    this.verbose = verbose;
    this.random = new Random(SEED);
  }

  /**
   * Creates a new logistic regression.
   */
  public LogisticRegression(DoubleVector theta) {
    this(0d, null, 1, false);
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
    return new DenseDoubleVector(new double[] { SIGMOID.get().apply(
        biasedFeatures.dot(theta)) });
  }

  public void train(DenseDoubleMatrix x, DenseDoubleVector y) {
    LogisticRegressionCostFunction fnc = new LogisticRegressionCostFunction(x,
        y, lambda);
    DoubleVector initialTheta = new DenseDoubleVector(x.getColumnCount() + 1);
    for (int i = 0; i < initialTheta.getLength(); i++) {
      initialTheta.set(i, (random.nextDouble() * 2) - 1d);
    }
    theta = minimizer.minimize(fnc, initialTheta, numIterations, verbose);
  }

  /**
   * Predicts the output by the given input.
   * 
   * @return the predicted vector consisting out of zeroes and ones.
   */
  public DoubleVector predict(DenseDoubleMatrix input, double threshold) {
    DoubleVector vec = new DenseDoubleMatrix(DenseDoubleVector.ones(input
        .getRowCount()), input).multiplyVector(theta);
    for (int i = 0; i < vec.getLength(); i++) {
      vec.set(i, SIGMOID.get().apply(vec.get(i)) > threshold ? 1.0d : 0.0d);
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
