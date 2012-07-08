package de.jungblut.regression;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

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
    DoubleVector trainModel = reg.trainModel(200, false);

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
    DenseDoubleMatrix xPoly = PolynomialRegression
        .createPolynomials(x, numPoly);
    PolynomialRegression reg = new PolynomialRegression(xPoly, y, 3.0d, true);
    DoubleVector trainModel = reg.trainModel(200, false);

    assertEquals(new DenseDoubleVector(new double[] { 6.915064696781851,
        0.8395937966930411 }),
        reg.predict(new DenseDoubleMatrix(new double[][] { { -15 }, { -29 } })));
    assertEquals(new DenseDoubleVector(new double[] { 11.217608314024684,
        12.449824953940029, 3.9687656618199205, 0.28643686866707746,
        1.9362841391625838, 0.45867528189634876, 0.7930260902910359,
        0.605370575894097, 0.11794150124685109 }), trainModel);
    assertEquals(4.3750765806456995d, reg.meanSquaredError(reg.predict(xPoly)));
  }

}
