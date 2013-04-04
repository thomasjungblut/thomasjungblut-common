package de.jungblut.regression;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.MathUtils;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;

public class PolynomialRegressionTest extends TestCase {

  DenseDoubleMatrix x = new DenseDoubleMatrix(new double[][] { { -15.9368 },
      { -29.1530 }, { 36.1895 }, { 37.4922 }, { -48.0588 }, { -8.9415 },
      { 15.3078 }, { -34.7063 }, { 1.3892 }, { -44.3838 }, { 7.0135 },
      { 22.7627 } });

  DenseDoubleVector y = new DenseDoubleVector(new double[] { 2.1343, 1.1733,
      34.3591, 36.8380, 2.8090, 2.1211, 14.7103, 2.6142, 3.7402, 3.7317,
      7.6277, 22.7524 });

  @Test
  public void testLinearRegression() {
    PolynomialRegression reg = new PolynomialRegression(x, y, 1.0, false);
    DoubleVector trainModel = reg.trainModel(new Fmincg(), 200, false);

    assertEquals(new DenseDoubleVector(new double[] { 7.571241358493101d,
        2.422334473628071d }),
        reg.predict(new DenseDoubleMatrix(new double[][] { { -15 }, { -29 } })));
    assertEquals(new DenseDoubleVector(new double[] { 13.087927306562776d,
        0.367779063204645d }), trainModel);
    assertEquals(44.74779284913223d, reg.meanSquaredError(reg.predict(x)));
  }

  @Test
  public void testPolynomialRegression() {
    int numPoly = 8;
    DenseDoubleMatrix xPoly = MathUtils.createPolynomials(x, numPoly);
    PolynomialRegression reg = new PolynomialRegression(xPoly, y, 3.0d, true);
    reg.trainModel(new Fmincg(), 200, false);
    DenseDoubleVector real = new DenseDoubleVector(new double[] {
        7.657335780995228, 2.632094730677398 });
    DoubleVector predict = reg.predict(new DenseDoubleMatrix(new double[][] {
        { -15 }, { -29 } }));
    assertEquals(0, real.subtract(predict).abs().sum(), 0.01);
    assertEquals(2.0734714302262063d, reg.meanSquaredError(reg.predict(xPoly)),
        0.1);
  }

}
