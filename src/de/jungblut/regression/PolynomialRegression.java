package de.jungblut.regression;

import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.normalize.Normalizer;
import de.jungblut.util.Tuple3;

public final class PolynomialRegression {

  private final DenseDoubleMatrix x;
  private final DenseDoubleVector y;
  private final double lambda;
  private final DenseDoubleVector mean;
  private final DenseDoubleVector stddev;
  private final boolean normalize;

  private DenseDoubleVector theta;

  public PolynomialRegression(DenseDoubleMatrix x, DenseDoubleVector y,
      double lambda, boolean normalize) {
    super();
    this.normalize = normalize;
    if (normalize) {
      Tuple3<DenseDoubleMatrix, DenseDoubleVector, DenseDoubleVector> featureNormalize = Normalizer
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

  public DenseDoubleVector trainModel(int numIterations) {
    RegressionCostFunction f = new RegressionCostFunction(x, y, lambda);
    DenseDoubleVector initialTheta = new DenseDoubleVector(
        x.getColumnCount() + 1);
    theta = Fmincg.minimizeFunction(f, initialTheta, numIterations, false);
    return theta;
  }

  public DenseDoubleVector predict(DenseDoubleMatrix input) {
    DenseDoubleMatrix in = input;
    if (normalize) {
      in = in.subtract(mean).divide(stddev);
    }
    return new DenseDoubleMatrix(DenseDoubleVector.ones(in.getRowCount()), in)
        .multiplyVector(theta);
  }

  public static DenseDoubleMatrix createPolynomials(DenseDoubleMatrix seed,
      int num) {
    DenseDoubleMatrix m = new DenseDoubleMatrix(seed.getRowCount(),
        seed.getColumnCount() * num);
    for (int c = 0; c < seed.getColumnCount(); c++) {
      m.setColumn(c, seed.getColumn(c));
    }
    int offset = seed.getColumnCount();
    // TODO experimental for seed matrix with more than 1 columns
    for (int col = offset; col < seed.getColumnCount() + 1; col++) {
      for (int i = 2; i < num + 1; i++) {
        DenseDoubleVector pow = seed.getColumnVector(col - offset).pow(i);
        m.setColumn(col * i - 1, pow.toArray());
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
    DenseDoubleVector trainModel = reg.trainModel(200);
    System.out
        .println("linear model: "
            + reg.predict(new DenseDoubleMatrix(new double[][] { { -15 },
                { -29 } })));
    System.out.println("theta: " + trainModel);

    DenseDoubleMatrix xPoly = createPolynomials(x, 8);
    reg = new PolynomialRegression(xPoly, y, 1.0, true);
    trainModel = reg.trainModel(200);
    System.out
        .println("8. polynomial model: "
            + reg.predict(new DenseDoubleMatrix(new double[][] { { -15 },
                { -29 } })));
    System.out.println("theta: " + trainModel);
  }
}
