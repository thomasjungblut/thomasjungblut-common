package de.jungblut.classification.nn;

import java.util.Random;

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
      new DenseDoubleVector(new double[] { 1, 1, 1, -1, -1, -1 }),
      new DenseDoubleVector(new double[] { 1, -1, 1, -1, -1, -1 }),
      new DenseDoubleVector(new double[] { 1, 1, 1, -1, -1, -1 }),
      new DenseDoubleVector(new double[] { -1, -1, 1, 1, 1, -1 }),
      new DenseDoubleVector(new double[] { -1, -1, 1, 1, -1, -1 }),
      new DenseDoubleVector(new double[] { -1, -1, 1, 1, 1, -1 }) };

  static Random rand;

  @Override
  protected void setUp() throws Exception {
    RBM.SEED = 0L;
    rand = new Random(RBM.SEED);
    MultilayerPerceptron.SEED = 0L;
  }

  @Test
  public void testCostFunction() {
    int hiddenUnits = 2;
    WeightMatrix pInput = new WeightMatrix(test[0].getDimension(),
        hiddenUnits + 1);
    RBMCostFunction fnc = new RBMCostFunction(test, 0, 1, hiddenUnits,
        new SigmoidActivationFunction(), TrainingType.CPU, 0d, 0d, 0d, rand);
    DoubleVector theta = GradientDescent.minimizeFunction(fnc,
        DenseMatrixFolder.foldMatrices(pInput.getWeights()), 0.01, 1e-5, 5000,
        false);
    int[][] pms = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(new int[] { test[0].getDimension(),
            hiddenUnits });
    DenseDoubleMatrix thetaMat = DenseMatrixFolder.unfoldMatrices(theta, pms)[0]
        .transpose();

    double[][] result = new double[][] {
        { 0.39721197943801945, 1.2791130851001233 },
        { 1.2597974616610634, -1.427360353321536 },
        { -0.6224364383658603, -1.669145908417724 },
        { -5.552514030854664, -5.37813097505567 },
        { -5.993342343796779, 0.2929667316179764 },
        { 2.2848235839601276, 2.4744165031434333 },
        { -1.476990981197591, -0.7751882643838997 } };

    assertEquals(0, thetaMat.subtract(new DenseDoubleMatrix(result)).sum(),
        1e-10);

  }
}
