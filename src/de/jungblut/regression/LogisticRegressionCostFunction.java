package de.jungblut.regression;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.tuple.Tuple;

/**
 * Logistic regression cost function to optimize with {@link Fmincg}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class LogisticRegressionCostFunction implements CostFunction {

  private final DoubleMatrix x;
  private final DoubleVector y;
  private final double lambda;
  private final int m;

  public LogisticRegressionCostFunction(DoubleMatrix x, DoubleVector y,
      double lambda) {
    // add a column of ones to handle the intercept term
    this.x = new DenseDoubleMatrix(DenseDoubleVector.ones(y.getLength()), x);
    this.y = y;
    this.lambda = lambda;
    this.m = y.getLength();
  }

  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

    DoubleVector sigmoidVector = sigmoidVector(x.multiplyVector(input));

    double j = (1.0d / m)
        * y.multiply(-1.d)
            .multiply(logVector(sigmoidVector))
            .subtract(
                (y.subtractFrom(1.0d)).multiply(logVector(sigmoidVector
                    .subtractFrom(1.0d)))).sum()
        + input.slice(1, input.getLength()).pow(2).sum() * lambda / (2.0d * m);

    DenseDoubleMatrix eye = DenseDoubleMatrix.eye(input.getLength());
    eye.set(0, 0, 0.0d);

    DoubleVector vec = eye.multiply(lambda).multiplyVector(input);

    DoubleVector gradient = (x.transpose().multiplyVector(
        sigmoidVector.subtract(y)).add(vec)).divide(m);

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
