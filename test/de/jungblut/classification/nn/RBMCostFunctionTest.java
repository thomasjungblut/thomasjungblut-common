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
        { 1.98234163269999, 1.3311164606850534 },
        { 1.3118127109468463, -1.2273748040174983 },
        { -0.7180453571713328, -1.7586609772655455 },
        { -5.556282185259046, -5.511691638574168 },
        { -6.1240333873434905, 1.597416589592292 },
        { 2.19066832486974, 2.384043983115857 },
        { -1.2156971891672392, -0.9055897930876634 }, };

    assertEquals(0, thetaMat.subtract(new DenseDoubleMatrix(result)).sum(),
        1e-10);

  }
}
