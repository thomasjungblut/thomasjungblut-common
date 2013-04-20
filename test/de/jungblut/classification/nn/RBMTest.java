package de.jungblut.classification.nn;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class RBMTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    RBMCostFunction.SEED = 0L;
    MultilayerPerceptron.SEED = 0L;
  }

  @Test
  public void testSingleRBM() throws Exception {
    RBM single = RBM.single(2);

    single.train(RBMCostFunctionTest.test, 0.01, 5000, false);

    DoubleVector predict = single.predictBinary(new DenseDoubleVector(
        new double[] { 0, 0, 0, 1, 1, 0 }));

    assertEquals(0, (int) predict.get(0));
    assertEquals(1, (int) predict.get(1));

    predict = single.predictBinary(new DenseDoubleVector(new double[] { 1, 1,
        0, 0, 0, 0 }));
    assertEquals(1, (int) predict.get(0));
    assertEquals(0, (int) predict.get(1));

  }

  @Test
  public void testStackedRBM() throws Exception {
    RBM single = RBM.stacked(4, 3, 2);

    single.train(RBMCostFunctionTest.test, 0.01, 5000, false);

    DoubleVector predict = single.predictBinary(new DenseDoubleVector(
        new double[] { 0, 0, 0, 1, 1, 0 }));

    assertEquals(1, (int) predict.get(0));
    assertEquals(1, (int) predict.get(1));

    predict = single.predictBinary(new DenseDoubleVector(new double[] { 1, 1,
        0, 0, 0, 0 }));
    assertEquals(1, (int) predict.get(0));
    assertEquals(0, (int) predict.get(1));

  }
}
