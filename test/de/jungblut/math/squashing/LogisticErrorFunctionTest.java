package de.jungblut.math.squashing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

public class LogisticErrorFunctionTest {

  @Test
  public void testSigmoidErrorMatrix() {
    DoubleMatrix y = new DenseDoubleMatrix(new double[] { 0d, 1d, 0d, 1d, 0d },
        1, 5);
    DoubleMatrix hypothesis = new DenseDoubleMatrix(new double[] { 0d, 0d, 0d,
        1d, 0d }, 1, 5);
    double error = new LogisticErrorFunction().calculateError(y, hypothesis);
    assertEquals(10d, error, 1e-4);
  }

  @Test
  public void testSigmoidErrorVector() {
    DoubleVector y = new DenseDoubleVector(new double[] { 0d, 1d, 0d, 1d, 0d });
    DoubleVector hypothesis = new DenseDoubleVector(new double[] { 0d, 0d, 0d,
        1d, 0d });
    double error = new LogisticErrorFunction().calculateError(y, hypothesis);
    assertEquals(10d, error, 1e-4);
  }

}
