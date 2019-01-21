package de.jungblut.math.loss;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.dense.DenseDoubleMatrix;

public class SquaredLossTest {

  @Test
  public void testSmeError() {
    DoubleMatrix y = new DenseDoubleMatrix(new double[] { 0d, 1d, 0d, 1d, 0d },
        1, 5);
    DoubleMatrix hypothesis = new DenseDoubleMatrix(new double[] { 0d, 0d, 0d,
        1d, 0d }, 1, 5);
    double error = new SquaredLoss().calculateLoss(y, hypothesis);
    assertEquals(1d, error, 1e-4);
  }

}
