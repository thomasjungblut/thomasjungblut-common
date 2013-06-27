package de.jungblut.classification.nn;

import java.util.Random;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.cuda.JCUDAMatrixUtils;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.AbstractMiniBatchCostFunction;
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
public final class RBMCostFunction extends AbstractMiniBatchCostFunction {

  private final ActivationFunction activationFunction;
  private final int[][] unfoldParameters;

  private final TrainingType type;
  private final double lambda;
  private final double visibleDropoutProbability;
  private final double hiddenDropoutProbability;
  private final Random random;

  // the trainingset should be activated and binarized already
  public RBMCostFunction(DoubleVector[] currentTrainingSet, int batchSize,
      int numThreads, int numHiddenUnits,
      ActivationFunction activationFunction, TrainingType type, double lambda,
      double visibleDropoutProbability, double hiddenDropoutProbability,
      long seed) {
    super(currentTrainingSet, batchSize, numThreads);
    this.activationFunction = activationFunction;
    this.type = type;
    this.lambda = lambda;
    this.visibleDropoutProbability = visibleDropoutProbability;
    this.hiddenDropoutProbability = hiddenDropoutProbability;
    this.random = new Random(seed);
    this.unfoldParameters = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(new int[] {
            currentTrainingSet[0].getDimension(), numHiddenUnits + 1 });
  }

  @Override
  protected Tuple<Double, DoubleVector> evaluateBatch(DoubleVector input,
      DoubleMatrix data) {
    // TODO unrolling is a problem when training in parallel, because the
    // matrices need to be unrolled in every thread- thus consume a multitude of
    // memory than really needed.

    // input contains the weights between the visible and the hidden units
    DenseDoubleMatrix theta = DenseMatrixFolder.unfoldMatrices(input,
        unfoldParameters)[0].transpose();

    // dropout the input if defined
    if (visibleDropoutProbability != 0d) {
      data = data.deepCopy();
      // compute dropout on the visible layer
      MultilayerPerceptronCostFunction.dropout(random, data,
          visibleDropoutProbability);
    }

    /*
     * POSITIVE PHASE
     */
    DoubleMatrix positiveHiddenProbs = activationFunction.apply(multiply(data,
        theta, false, false));
    // set out hidden bias back to 1
    positiveHiddenProbs.setColumnVector(0,
        DenseDoubleVector.ones(positiveHiddenProbs.getRowCount()));
    DoubleMatrix positiveAssociations = multiply(data, positiveHiddenProbs,
        true, false);
    /*
     * END OF POSITIVE PHASE
     */
    binarize(random, positiveHiddenProbs);
    if (hiddenDropoutProbability != 0d) {
      // compute dropout on the hidden layer
      MultilayerPerceptronCostFunction.dropout(random, positiveHiddenProbs,
          hiddenDropoutProbability);
    }
    /*
     * START NEGATIVE PHASE
     */
    DoubleMatrix negativeData = activationFunction.apply(multiply(
        positiveHiddenProbs, theta, false, true));
    negativeData.setColumnVector(0,
        DenseDoubleVector.ones(negativeData.getRowCount()));
    DoubleMatrix negativeHiddenProbs = activationFunction.apply(multiply(
        negativeData, theta, false, false));
    negativeHiddenProbs.setColumnVector(0,
        DenseDoubleVector.ones(negativeHiddenProbs.getRowCount()));
    DoubleMatrix negativeAssociations = multiply(negativeData,
        negativeHiddenProbs, true, false);
    /*
     * END OF NEGATIVE PHASE
     */

    // measure a very simple reconstruction error
    double j = data.subtract(negativeData).pow(2).sum();

    // calculate the approx. gradient and make it negative.
    DoubleMatrix thetaGradient = positiveAssociations
        .subtract(negativeAssociations).divide(data.getRowCount()).multiply(-1);

    // calculate the weight decay and apply it
    if (lambda != 0d) {
      DoubleVector bias = thetaGradient.getColumnVector(0);
      thetaGradient = thetaGradient.add(thetaGradient.multiply(lambda
          / data.getRowCount()));
      thetaGradient.setColumnVector(0, bias);
    }

    // transpose the gradient, because we transposed theta at the top.
    return new Tuple<Double, DoubleVector>(j,
        DenseMatrixFolder.foldMatrices((DenseDoubleMatrix) thetaGradient
            .transpose()));
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

  static DoubleVector[] binarize(Random r, DoubleVector[] hiddenActivations) {
    for (int i = 0; i < hiddenActivations.length; i++) {
      binarize(r, hiddenActivations[i]);
    }
    return hiddenActivations;
  }

  static DoubleMatrix binarize(Random r, DoubleMatrix hiddenActivations) {
    for (int i = 0; i < hiddenActivations.getRowCount(); i++) {
      for (int j = 0; j < hiddenActivations.getColumnCount(); j++) {
        hiddenActivations.set(i, j,
            hiddenActivations.get(i, j) > r.nextDouble() ? 1d : 0d);
      }
    }
    return hiddenActivations;
  }

  static DoubleVector binarize(Random r, DoubleVector v) {
    for (int j = 0; j < v.getDimension(); j++) {
      v.set(j, v.get(j) > r.nextDouble() ? 1d : 0d);
    }
    return v;
  }

}
