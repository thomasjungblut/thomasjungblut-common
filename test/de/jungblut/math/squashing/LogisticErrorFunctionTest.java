package de.jungblut.math.squashing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.dense.DenseDoubleMatrix;

public class LogisticErrorFunctionTest {

  @Test
  public void testSigmoidError() {
    DoubleMatrix y = new DenseDoubleMatrix(new double[] { 0d, 1d, 0d, 1d, 0d },
        1, 5);
    DoubleMatrix hypothesis = new DenseDoubleMatrix(new double[] { 0d, 0d, 0d,
        1d, 0d }, 1, 5);
    double error = new LogisticErrorFunction().calculateError(y, hypothesis);
    assertEquals(10d, error, 1e-4);
  }

}
