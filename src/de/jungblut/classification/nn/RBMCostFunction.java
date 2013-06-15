package de.jungblut.classification.nn;

import java.util.Random;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.cuda.JCUDAMatrixUtils;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.minimize.GradientDescent;
import de.jungblut.math.tuple.Tuple;

/**
 * Restricted Boltzmann machine implementation using Contrastive Divergence 1
 * (CD1). This algorithm is based on what has been teached by Prof. Hinton in
 * the Coursera course "Neural Networks for Machine Learning" '12. This is an
 * unsupervised learning algorithm to train high level feature detectors. NOTE:
 * Sutskever and Tieleman have shown that it is not following the gradient of
 * any function (Sutskever and Tieleman, 2010). So this isn't minimizable using
 * line searching optimizers like {@link Fmincg}, {@link GradientDescent} is
 * doing a great job though as it doesn't care if you're moving into the right
 * direction down hill.
 * 
 * @author thomas.jungblut
 * 
 */
public final class RBMCostFunction implements CostFunction {

  // test seed
  static long SEED = System.currentTimeMillis();

  private final DenseDoubleMatrix x;
  private final ActivationFunction activationFunction;
  private int[][] unfoldParameters;
  private DenseDoubleVector ones;

  private TrainingType type;

  public RBMCostFunction(DenseDoubleMatrix x, int numHiddenUnits,
      ActivationFunction activationFunction, TrainingType type) {
    this.activationFunction = activationFunction;
    this.type = type;
    this.ones = DenseDoubleVector.ones(x.getRowCount());
    this.x = new DenseDoubleMatrix(ones, x);
    this.unfoldParameters = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(new int[] { x.getColumnCount(), numHiddenUnits });
  }

  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {
    // input contains the weights between the visible and the hidden units
    DenseDoubleMatrix[] thetas = DenseMatrixFolder.unfoldMatrices(input,
        unfoldParameters);
    DenseDoubleMatrix[] thetaGradients = new DenseDoubleMatrix[thetas.length];

    DoubleMatrix hiddenActivations = activationFunction.apply(multiply(x,
        thetas[0], false, true));
    DoubleMatrix positiveAssociations = multiply(x, hiddenActivations, true,
        false);
    // binarize to 1 or 0
    binarize(hiddenActivations);

    // start reconstructing the input
    DoubleMatrix fantasy = activationFunction.apply(multiply(hiddenActivations,
        thetas[0], false, false));
    // set out fantasy bias back to 1
    fantasy.setColumnVector(0, ones);
    DoubleMatrix hiddenFantasyActivations = activationFunction.apply(multiply(
        fantasy, thetas[0], false, true));

    DoubleMatrix negativeAssociations = fantasy.transpose().multiply(
        hiddenFantasyActivations);

    double j = x.subtract(fantasy).pow(2).sum();

    // calculate the approx. gradient and make it negative, so it works with
    // gradient descent
    thetaGradients[0] = (DenseDoubleMatrix) positiveAssociations
        .subtract(negativeAssociations).transpose().divide(x.getRowCount())
        .multiply(-1d);

    return new Tuple<Double, DoubleVector>(j,
        DenseMatrixFolder.foldMatrices(thetaGradients));
  }

  // this is a test design opposed to the inheritance design of the multilayer
  // perceptron costfunction.
  private DoubleMatrix multiply(DoubleMatrix a1, DoubleMatrix a2,
      boolean a1Transpose, boolean a2Transpose) {
    switch (type) {
      case CPU:
        return multiplyCPU(a1, a2, a1Transpose, a2Transpose);
      case GPU:
        return multiplyGPU(a1, a2, a1Transpose, a2Transpose);
    }
    throw new IllegalArgumentException(
        "Trainingtype couldn't be anticipated by switch.");
  }

  private static DoubleMatrix multiplyCPU(DoubleMatrix a1, DoubleMatrix a2,
      boolean a1Transpose, boolean a2Transpose) {
    a2 = a2Transpose ? a2.transpose() : a2;
    a1 = a1Transpose ? a1.transpose() : a1;
    return a1.multiply(a2);
  }

  private static DoubleMatrix multiplyGPU(DoubleMatrix a1, DoubleMatrix a2,
      boolean a1Transpose, boolean a2Transpose) {
    return JCUDAMatrixUtils.multiply((DenseDoubleMatrix) a1,
        (DenseDoubleMatrix) a2, a1Transpose, a2Transpose);
  }

  int[][] getUnfoldParameters() {
    return this.unfoldParameters;
  }

  static void binarize(DoubleMatrix hiddenActivations) {
    final Random r = new Random(SEED);
    for (int i = 0; i < hiddenActivations.getRowCount(); i++) {
      for (int j = 0; j < hiddenActivations.getColumnCount(); j++) {
        hiddenActivations.set(i, j,
            hiddenActivations.get(i, j) > r.nextDouble() ? 1d : 0d);
      }
    }
  }

  static void binarize(DoubleVector v) {
    final Random r = new Random(SEED);
    for (int j = 0; j < v.getDimension(); j++) {
      v.set(j, v.get(j) > r.nextDouble() ? 1d : 0d);
    }
  }
}
