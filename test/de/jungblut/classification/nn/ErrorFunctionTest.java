package de.jungblut.classification.nn;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.dense.DenseDoubleMatrix;

public class ErrorFunctionTest extends TestCase {

  @Test
  public void testSigmoidError() {
    DoubleMatrix y = new DenseDoubleMatrix(new double[] { 0d, 1d, 0d, 1d, 0d },
        1, 5);
    DoubleMatrix hypothesis = new DenseDoubleMatrix(new double[] { 0d, 0d, 0d,
        1d, 0d }, 1, 5);
    double error = ErrorFunction.SIGMOID_ERROR.getError(y, hypothesis);
    assertEquals(10d, error);
  }

  @Test
  public void testSoftmaxError() {
    DoubleMatrix y = new DenseDoubleMatrix(new double[][] {
        { 0d, 1d, 0.5d, 1d, 0.2d }, { 1d, 0d, 0.5d, 0d, 0.8d } });
    DoubleMatrix hypothesis = new DenseDoubleMatrix(new double[][] {
        { 0d, 0d, 0d, 1d, 0d }, { 1d, 1d, 1d, 0d, 0d } });
    double error = ErrorFunction.SOFTMAX_ERROR.getError(y, hypothesis);
    assertEquals(25d, error);
  }

  @Test
  public void testSmeError() {
    DoubleMatrix y = new DenseDoubleMatrix(new double[] { 0d, 1d, 0d, 1d, 0d },
        1, 5);
    DoubleMatrix hypothesis = new DenseDoubleMatrix(new double[] { 0d, 0d, 0d,
        1d, 0d }, 1, 5);
    double error = ErrorFunction.SQUARED_MEAN_ERROR.getError(y, hypothesis);
    assertEquals(1d, error);
  }

  @Test
  public void testLogMatrix() {
    DoubleMatrix y = new DenseDoubleMatrix(
        new double[][] { { 0d, 1d, 0.5d, Double.NaN, 0.2d,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY } });
    DoubleMatrix mat = ErrorFunction.logMatrix(y);
    assertEquals(-10d, mat.get(0, 0));
    assertEquals(0d, mat.get(0, 1));
    assertEquals(-0.6931471805599453, mat.get(0, 2));
    assertEquals(0d, mat.get(0, 3));
    assertEquals(-1.6094379124341003, mat.get(0, 4));
    assertEquals(0d, mat.get(0, 5));
    assertEquals(0d, mat.get(0, 6));
  }
}
