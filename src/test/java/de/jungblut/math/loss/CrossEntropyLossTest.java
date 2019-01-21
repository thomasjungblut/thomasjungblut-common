package de.jungblut.math.loss;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.dense.DenseDoubleMatrix;

public class CrossEntropyLossTest {
  @Test
  public void testSoftmaxError() {
    DoubleMatrix y = new DenseDoubleMatrix(new double[][] {
        { 0d, 1d, 0.5d, 1d, 0.2d }, { 1d, 0d, 0.5d, 0d, 0.8d } });
    DoubleMatrix hypothesis = new DenseDoubleMatrix(new double[][] {
        { 0d, 0d, 0d, 1d, 0d }, { 1d, 1d, 1d, 0d, 0d } });
    double error = new CrossEntropyLoss().calculateLoss(y, hypothesis);
    assertEquals(12.5d, error, 1e-4);
  }

}
