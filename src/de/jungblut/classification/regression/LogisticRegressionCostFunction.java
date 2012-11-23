package de.jungblut.classification.regression;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.math.tuple.Tuple;

/**
 * Logistic regression cost function to optimize with an arbitrary
 * {@link Minimizer}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class LogisticRegressionCostFunction implements CostFunction {

  private final DoubleMatrix x;
  private final DoubleVector y;
  private final double lambda;
  private final int m;
  private final DoubleVector negatedY;
  private final DoubleVector substractedY;

  public LogisticRegressionCostFunction(DoubleMatrix x, DoubleVector y,
      double lambda) {
    if (!x.isSparse()) {
      // add a column of ones to handle the intercept term
      this.x = new DenseDoubleMatrix(DenseDoubleVector.ones(y.getLength()), x);
    } else {
      this.x = new SparseDoubleColumnMatrix(DenseDoubleVector.ones(y
          .getLength()), x);
    }
    this.y = y;
    this.lambda = lambda;
    this.m = y.getLength();
    this.negatedY = y.multiply(-1.d);
    this.substractedY = y.subtractFrom(1.0d);
  }

  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

    DoubleVector sigmoidVector = sigmoidVector(x.multiplyVector(input));
    double reg = input.slice(1, input.getLength()).pow(2).sum() * lambda
        / (2.0d * m);
    DoubleVector prob = negatedY.multiply(logVector(sigmoidVector));
    prob = prob.subtract(substractedY.multiply(logVector(sigmoidVector
        .subtractFrom(1.0d))));
    // -1/m * y * log(sigmoid(x'*theta))-(1-y * log(1-sigmoid(x'*theta))) + reg
    double j = (1.0d / m) * prob.sum() + reg;

    DenseDoubleMatrix eye = DenseDoubleMatrix.eye(input.getLength());
    eye.set(0, 0, 0.0d);

    DoubleVector regGradient = eye.multiply(lambda).multiplyVector(input);

    DoubleVector gradient = (x.transpose().multiplyVector(
        sigmoidVector.subtract(y)).add(regGradient)).divide(m);

    return new Tuple<>(j, gradient);
  }

  /*
   * Some static helpers for functions
   */

  static double sigmoid(double input) {
    return 1.0 / (1.0 + Math.exp(-input));
  }

  static DoubleVector sigmoidVector(DoubleVector v) {
    DoubleVector vx = new DenseDoubleVector(v.getLength());

    for (int i = 0; i < v.getLength(); i++) {
      vx.set(i, sigmoid(v.get(i)));
    }

    return vx;
  }

  static DoubleVector logVector(DoubleVector input) {
    DoubleVector vx = new DenseDoubleVector(input.getLength());

    for (int i = 0; i < input.getLength(); i++) {
      vx.set(i, Math.log(input.get(i)));
    }

    return vx;
  }

}
