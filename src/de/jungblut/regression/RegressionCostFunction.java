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

  public static void main(String[] args) {
    // ml-class test
    DenseDoubleMatrix x = new DenseDoubleMatrix(new double[][] { { -15.9368 },
        { -29.1530 }, { 36.1895 }, { 37.4922 }, { -48.0588 }, { -8.9415 },
        { 15.3078 }, { -34.7063 }, { 1.3892 }, { -44.3838 }, { 7.0135 },
        { 22.7627 } });

    DenseDoubleVector y = new DenseDoubleVector(new double[] { 2.1343, 1.1733,
        34.3591, 36.8380, 2.8090, 2.1211, 14.7103, 2.6142, 3.7402, 3.7317,
        7.6277, 22.7524 });

    double lambda = 1;

    RegressionCostFunction f = new RegressionCostFunction(x, y, lambda);
    Tuple<Double, DenseDoubleVector> res = f
        .evaluateCost(new DenseDoubleVector(new double[] { 1, 1 }));
    System.out
        .println("Should be [cost=303.9521283241666, gradient=[-15.303049999999999, 598.1679597016666]]");
    System.out.println(res);

  }

}
