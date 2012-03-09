package de.jungblut.regression;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.normalize.Normalizer;
import de.jungblut.util.Tuple3;

public final class PolynomialRegression {

  private final DenseDoubleMatrix x;
  private final DoubleVector y;
  private final double lambda;
  private final DoubleVector mean;
  private final DoubleVector stddev;
  private final boolean normalize;

  private DoubleVector theta;

  public PolynomialRegression(DenseDoubleMatrix x, DenseDoubleVector y,
      double lambda, boolean normalize) {
    super();
    this.normalize = normalize;
    if (normalize) {
      Tuple3<DenseDoubleMatrix, DoubleVector, DoubleVector> featureNormalize = Normalizer
          .featureNormalize(x);
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

  public DoubleVector trainModel(int numIterations, boolean verbose) {
    RegressionCostFunction f = new RegressionCostFunction(x, y, lambda);
    DoubleVector initialTheta = new DenseDoubleVector(
        x.getColumnCount() + 1, 1.0d);
    theta = Fmincg.minimizeFunction(f, initialTheta, numIterations, verbose);
    return theta;
  }

  public DoubleVector predict(DenseDoubleMatrix input) {
    DenseDoubleMatrix in = input;
    if (normalize) {
      in = in.subtract(mean).divide(stddev);
    }
    return new DenseDoubleMatrix(DenseDoubleVector.ones(in.getRowCount()), in)
        .multiplyVector(theta);
  }

  // mean squared error
  public double error(DoubleVector prediction) {
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

  public DenseDoubleMatrix getX() {
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

  public boolean isNormalize() {
    return normalize;
  }

  public static DenseDoubleMatrix createPolynomials(DenseDoubleMatrix seed,
      int num) {
    if (num == 1)
      return seed;
    DenseDoubleMatrix m = new DenseDoubleMatrix(seed.getRowCount(),
        seed.getColumnCount() * num);
    int index = 0;
    for (int c = 0; c < m.getColumnCount(); c+=num) {
      double[] column = seed.getColumn(index++);
      m.setColumn(c, column);
      for (int i = 2; i < num + 1; i++) {
        DoubleVector pow = new DenseDoubleVector(column).pow(i);
        m.setColumn(c + i - 1, pow.toArray());
      }
    }
    return m;
  }

  public static void main(String[] args) {
    DenseDoubleMatrix x = new DenseDoubleMatrix(new double[][] { { -15.9368 },
        { -29.1530 }, { 36.1895 }, { 37.4922 }, { -48.0588 }, { -8.9415 },
        { 15.3078 }, { -34.7063 }, { 1.3892 }, { -44.3838 }, { 7.0135 },
        { 22.7627 } });

    DenseDoubleVector y = new DenseDoubleVector(new double[] { 2.1343, 1.1733,
        34.3591, 36.8380, 2.8090, 2.1211, 14.7103, 2.6142, 3.7402, 3.7317,
        7.6277, 22.7524 });

    PolynomialRegression reg = new PolynomialRegression(x, y, 1.0, false);
    DoubleVector trainModel = reg.trainModel(200, false);
    System.out
        .println("linear model: "
            + reg.predict(new DenseDoubleMatrix(new double[][] { { -15 },
                { -29 } })));
    System.out.println("theta: " + trainModel);
    System.out.println(reg.error(reg.predict(x)));

    int numPoly = 8;

    DenseDoubleMatrix xPoly = createPolynomials(x, numPoly);
    reg = new PolynomialRegression(xPoly, y, 3.0d, true);
    trainModel = reg.trainModel(200, false);
    System.out
        .println(numPoly
            + ". polynomial model: "
            + reg.predict(new DenseDoubleMatrix(new double[][] { { -15 },
                { -29 } })));
    System.out.println("theta: " + trainModel);
    System.out.println(reg.error(reg.predict(xPoly)));

    // GnuPlot.plot(x, y, trainModel, numPoly, reg.mean, reg.stddev);

  }
}
