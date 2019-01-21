package de.jungblut.classification.regression;

import static de.jungblut.math.activation.ActivationFunctionSelector.SIGMOID;

import java.util.Iterator;
import java.util.Random;

import com.google.common.base.Preconditions;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
import de.jungblut.math.sparse.SparseDoubleVector;

public final class LogisticRegression extends AbstractClassifier {

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
    this.random = new Random();
  }

  /**
   * Creates a new logistic regression by already existing parameters.
   */
  public LogisticRegression(DoubleVector theta) {
    this(0d, null, 1, false);
    this.theta = theta;
  }

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    Preconditions.checkArgument(features.length == outcome.length,
        "Features and Outcomes need to match in length!");
    DoubleMatrix x = null;
    DoubleMatrix y = null;
    // add the bias
    if (features[0].isSparse()) {
      x = new SparseDoubleRowMatrix(DenseDoubleVector.ones(features.length),
          new SparseDoubleRowMatrix(features));
    } else {
      x = new DenseDoubleMatrix(DenseDoubleVector.ones(features.length),
          new DenseDoubleMatrix(features));
    }
    if (outcome[0].isSparse()) {
      y = new SparseDoubleRowMatrix(outcome);
    } else {
      y = new DenseDoubleMatrix(outcome);
    }
    // transpose y to get a faster lookup in the cost function
    y = y.transpose();

    LogisticRegressionCostFunction cnf = new LogisticRegressionCostFunction(x,
        y, lambda);

    // random init theta
    theta = new DenseDoubleVector(x.getColumnCount() * y.getRowCount());
    for (int i = 0; i < theta.getDimension(); i++) {
      theta.set(i, (random.nextDouble() * 2) - 1d);
    }
    theta = minimizer.minimize(cnf, theta, numIterations, verbose);
  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    if (features.isSparse()) {
      SparseDoubleVector tmp = new SparseDoubleVector(
          features.getDimension() + 1);
      tmp.set(0, 1d);
      Iterator<DoubleVectorElement> iterateNonZero = features.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        tmp.set(next.getIndex() + 1, next.getValue());
      }
      features = tmp;
    } else {
      features = new DenseDoubleVector(1d, features.toArray());
    }
    return new DenseDoubleVector(new double[] { SIGMOID.get().apply(
        features.dot(theta)) });
  }

  /**
   * @return the learned weights.FSO
   */
  public DoubleVector getTheta() {
    return theta;
  }

  void setRandom(Random random) {
    this.random = random;
  }
}
