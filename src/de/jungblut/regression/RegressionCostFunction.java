package de.jungblut.regression;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.util.Tuple;

public class RegressionCostFunction implements CostFunction {

  private final DenseDoubleMatrix x;
  private final DoubleVector y;
  private final int m;
  private final double lambda;

  public RegressionCostFunction(DenseDoubleMatrix x, DoubleVector y,
      double lambda) {
    // add ones to the first column
    this.x = new DenseDoubleMatrix(DenseDoubleVector.ones(x.getRowCount()), x);
    this.y = y;
    this.m = y.getLength();
    this.lambda = lambda;
  }

  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector theta) {

    DoubleVector predictions = x.multiplyVector(theta);
    DoubleVector sqrErrors = predictions.subtract(y).pow(2);

    // (sum(sqrErrors)/2/m) + lambda * (sum(theta(2:end).^2)/2/m);
    double j = (sqrErrors.sum() / 2 / m) + lambda
        * (theta.slice(2, theta.getLength()).pow(2).sum() / 2 / m);

    DoubleVector gradient = new DenseDoubleVector(theta.getLength());
    for (int i = 0; i < theta.getLength(); i++) {
      gradient.set(i,
          (predictions.subtract(y).multiply(x.getColumnVector(i))).sum() / m);
      if (i > 1)
        gradient.set(i, gradient.get(i) + lambda * (theta.get(i) / m));
    }

    return new Tuple<Double, DoubleVector>(j, gradient);
  }

}
