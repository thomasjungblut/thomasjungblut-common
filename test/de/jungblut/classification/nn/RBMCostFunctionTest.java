package de.jungblut.classification.nn;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.SigmoidActivationFunction;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.GradientDescent;

public class RBMCostFunctionTest extends TestCase {

  // testcase from
  // https://github.com/echen/restricted-boltzmann-machines
  static DoubleVector[] test = new DoubleVector[] {
      new DenseDoubleVector(new double[] { 1, 1, 1, 0, 0, 0 }),
      new DenseDoubleVector(new double[] { 1, 0, 1, 0, 0, 0 }),
      new DenseDoubleVector(new double[] { 1, 1, 1, 0, 0, 0 }),
      new DenseDoubleVector(new double[] { 0, 0, 1, 1, 1, 0 }),
      new DenseDoubleVector(new double[] { 0, 0, 1, 1, 0, 0 }),
      new DenseDoubleVector(new double[] { 0, 0, 1, 1, 1, 0 }) };

  @Override
  protected void setUp() throws Exception {
    RBM.SEED = 0L;
    MultilayerPerceptron.SEED = 0L;
  }

  @Test
  public void testCostFunction() {
    int hiddenUnits = 2;
    WeightMatrix pInput = new WeightMatrix(test[0].getDimension(), hiddenUnits);
    RBMCostFunction fnc = new RBMCostFunction(test, 0, 1, hiddenUnits,
        new SigmoidActivationFunction(), TrainingType.CPU, 0d, 0d, 0d);
    DoubleVector theta = GradientDescent.minimizeFunction(fnc,
        DenseMatrixFolder.foldMatrices(pInput.getWeights()), 0.01, 1e-5, 5000,
        false);
    int[][] pms = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(new int[] { test[0].getDimension(),
            hiddenUnits });
    DenseDoubleMatrix thetaMat = DenseMatrixFolder.unfoldMatrices(theta, pms)[0]
        .transpose();

    double[][] result = new double[][] {
        { 0.6527712259694591, 0.2738610444800008 },
        { 0.09138766480276334, -0.09571474054615228 },
        { 0.36640026896582517, -1.1966433764873787 },
        { 1.5590449381205493, 1.1927390227606613 },
        { -0.23472376807507878, 0.3139670828894665 },
        { -0.27143967646820316, 0.04801478658623357 },
        { -1.3602144875255062, -0.5884501745982235 } };

    assertEquals(0, thetaMat.subtract(new DenseDoubleMatrix(result)).sum(),
        1e-10);

  }
}
