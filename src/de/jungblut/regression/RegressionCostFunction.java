package de.jungblut.regression;

import static de.jungblut.math.MatrixUtils.sum;
import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.util.Tuple;

public class RegressionCostFunction implements CostFunction {

  private final DenseDoubleMatrix x;
  private final DenseDoubleVector y;
  private final int m;
  private final double lambda;

  public RegressionCostFunction(DenseDoubleMatrix x, DenseDoubleVector y,
      double lambda) {
    // add ones to the first column
    this.x = new DenseDoubleMatrix(DenseDoubleVector.ones(x.getRowCount()), x);
    this.y = y;
    this.m = y.getLength();
    this.lambda = lambda;
  }

  @Override
  public Tuple<Double, DenseDoubleVector> evaluateCost(DenseDoubleVector theta) {

    DenseDoubleVector predictions = x.multiplyVector(theta);
    DenseDoubleVector sqrErrors = predictions.subtract(y).pow(2);

    // (sum(sqrErrors)/2/m) + lambda * (sum(theta(2:end).^2)/2/m);
    double j = (sum(sqrErrors) / 2 / m) + lambda
        * (sum(theta.slice(2, theta.getLength()).pow(2)) / 2 / m);

    DenseDoubleVector gradient = new DenseDoubleVector(theta.getLength());
    for (int i = 0; i < theta.getLength(); i++) {
      gradient.set(i,
          sum((predictions.subtract(y).multiply(x.getColumnVector(i)))) / m);
      if (i > 1)
        gradient.set(i, gradient.get(i) + lambda * (theta.get(i) / m));
    }

    return new Tuple<Double, DenseDoubleVector>(j, gradient);
  }

}
