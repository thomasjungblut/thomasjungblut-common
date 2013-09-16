package de.jungblut.classification.nn;

import java.util.Random;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.cuda.JCUDAMatrixUtils;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.AbstractMiniBatchCostFunction;
import de.jungblut.math.minimize.CostGradientTuple;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.squashing.ErrorFunction;

/**
 * Neural network costfunction for a multilayer perceptron.
 * 
 * @author thomas.jungblut
 */
public final class MultilayerPerceptronCostFunction extends
    AbstractMiniBatchCostFunction {

  private final double lambda;
  private final int[] layerSizes;
  private final int[][] unfoldParameters;
  private final ActivationFunction[] activations;
  private final ErrorFunction error;
  private final TrainingType trainingType;
  private final double visibleDropoutProbability;
  private final double hiddenDropoutProbability;
  private final Random rnd;

  public MultilayerPerceptronCostFunction(MultilayerPerceptron network,
      DoubleVector[] features, DoubleVector[] outcome) {
    super(features, outcome, network.getMiniBatchSize(), network
        .getBatchParallelism(), network.isStochastic());
    this.lambda = network.getLambda();
    this.layerSizes = network.getLayers();
    this.unfoldParameters = computeUnfoldParameters(layerSizes);
    this.activations = network.getActivations();
    this.error = network.getErrorFunction();
    this.trainingType = network.getTrainingType();
    this.visibleDropoutProbability = network.getVisibleDropoutProbability();
    this.hiddenDropoutProbability = network.getHiddenDropoutProbability();
    this.rnd = new Random();
  }

  @Override
  protected CostGradientTuple evaluateBatch(DoubleVector theta,
      DoubleMatrix featureBatch, DoubleMatrix outcomeBatch) {
    return compute(theta, featureBatch, outcomeBatch);
  }

  /**
   * Do a full forward pass and backpropagate the error.
   * 
   * @param input the input parameters (theta).
   * @param x the features.
   * @param y the outcome.
   * @return a tuple of cost and gradient.
   */
  private CostGradientTuple compute(DoubleVector input, DoubleMatrix x,
      DoubleMatrix y) {
    final int m = x.getRowCount();
    DoubleMatrix[] thetas = DenseMatrixFolder.unfoldMatrices(input,
        unfoldParameters);
    DoubleMatrix[] thetaGradients = new DoubleMatrix[thetas.length];

    // start forward propagation
    // we compute the aX activations for all layers
    DoubleMatrix[] ax = new DoubleMatrix[layerSizes.length];
    // for zX we constantly null the zero index, since it has no use.
    // zX are the values before activation
    DoubleMatrix[] zx = new DoubleMatrix[layerSizes.length];

    dropoutVisibleLayer(x, ax);

    forwardPropagate(thetas, ax, zx);

    double regularization = calculateRegularization(thetas, m);

    DoubleMatrix[] deltaX = backwardPropagate(y, thetas, ax, zx);

    calculateGradients(thetas, thetaGradients, ax, deltaX, m);

    // calculate our cost (error in the last layer)
    double j = (1.0d / m) * error.calculateError(y, ax[layerSizes.length - 1])
        + regularization;

    return new CostGradientTuple(j,
        DenseMatrixFolder.foldMatrices(thetaGradients));
  }

  public void forwardPropagate(DoubleMatrix[] thetas, DoubleMatrix[] ax,
      DoubleMatrix[] zx) {
    for (int i = 1; i < layerSizes.length; i++) {
      zx[i] = multiply(ax[i - 1], thetas[i - 1], false, true);

      if (i < (layerSizes.length - 1)) {
        ax[i] = new DenseDoubleMatrix(DenseDoubleVector.ones(zx[i]
            .getRowCount()), activations[i].apply(zx[i]));
        if (hiddenDropoutProbability > 0d) {
          // compute dropout for ax[i]
          dropout(rnd, ax[i], hiddenDropoutProbability);
        }
      } else {
        // the output doesn't need a bias
        ax[i] = activations[i].apply(zx[i]);
      }
    }
  }

  public DoubleMatrix[] backwardPropagate(DoubleMatrix y,
      DoubleMatrix[] thetas, DoubleMatrix[] ax, DoubleMatrix[] zx) {
    // now backpropagate the error backwards by calculating the deltas.
    // also here we are following the math equations and nulling out the 0th
    // entry.
    DoubleMatrix[] deltaX = new DoubleMatrix[layerSizes.length];
    // set the last delta to the difference of outcome and prediction
    deltaX[deltaX.length - 1] = ax[layerSizes.length - 1].subtract(y);
    // compute the deltas onto the input layer
    for (int i = (layerSizes.length - 2); i > 0; i--) {
      DoubleMatrix slice = thetas[i].slice(0, thetas[i].getRowCount(), 1,
          thetas[i].getColumnCount());
      deltaX[i] = multiply(deltaX[i + 1], slice, false, false);
      // apply the gradient of the activations
      deltaX[i] = deltaX[i].multiplyElementWise(activations[i].gradient(zx[i]));
    }
    return deltaX;
  }

  public void calculateGradients(DoubleMatrix[] thetas,
      DoubleMatrix[] thetaGradients, DoubleMatrix[] ax, DoubleMatrix[] deltaX,
      final int m) {
    // calculate the gradients of the weights
    for (int i = 0; i < thetaGradients.length; i++) {
      DoubleMatrix gradDXA = multiply(deltaX[i + 1], ax[i], true, false);
      if (m != 1) {
        thetaGradients[i] = (DenseDoubleMatrix) gradDXA.divide(m);
      } else {
        thetaGradients[i] = (DenseDoubleMatrix) gradDXA;
      }
      if (lambda != 0d) {
        thetaGradients[i] = (DenseDoubleMatrix) thetaGradients[i]
            .add((thetas[i].multiply(lambda / m)));
        // subtract the regularized bias
        DoubleVector regBias = thetas[i]
            .slice(0, thetas[i].getRowCount(), 0, 1).multiply(lambda / m)
            .getColumnVector(0);
        thetaGradients[i].setColumnVector(0, regBias);
      }
    }
  }

  public double calculateRegularization(DoubleMatrix[] thetas, final int m) {
    double regularization = 0d;
    // only calculate the regularization term if lambda is not 0
    if (lambda != 0d) {
      for (DoubleMatrix theta : thetas) {
        regularization += (theta.slice(0, theta.getRowCount(), 1,
            theta.getColumnCount())).pow(2).sum();
      }
      regularization = (lambda / (2.0d * m)) * regularization;
    }
    return regularization;
  }

  public void dropoutVisibleLayer(DoubleMatrix x, DoubleMatrix[] ax) {
    // for the first weights, we don't need to compute Z
    if (visibleDropoutProbability > 0d) {
      // compute dropout for ax[0], copy X to not alter internal
      // representation
      ax[0] = x.deepCopy();
      dropout(rnd, ax[0], visibleDropoutProbability);
    } else {
      ax[0] = x;
    }
  }

  private DoubleMatrix multiply(DoubleMatrix a1, DoubleMatrix a2,
      boolean a1Transpose, boolean a2Transpose) {
    switch (trainingType) {
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

  /**
   * Calculates the unfold parameters to unroll a learned theta vector in their
   * matrix.
   * 
   * @param layerSizes the layer size that the {@link MultilayerPerceptron} got
   *          instantiated with.
   * @return the unfold parameters to feed {@link DenseMatrixFolder} with.
   */
  public static int[][] computeUnfoldParameters(int[] layerSizes) {
    // the weights that need to be learned are always one less than the number
    // of layers
    int[][] unfoldParameters = new int[layerSizes.length - 1][];
    for (int i = 0; i < unfoldParameters.length; i++) {
      // note that this will never lead to array index out of bound execeptions
      // because the layer size is always one more than the unfold size.
      // also don't forget to add the bias unit on the cols of the matrices.
      unfoldParameters[i] = new int[] { layerSizes[i + 1], layerSizes[i] + 1 };
    }
    return unfoldParameters;
  }

  /**
   * Computes dropout for the activations matrix. Each element for each row has
   * the similar probability p to be "dropped out" (set to 0) of the
   * computation. This way, the network does learn to not rely on other units
   * thus learning to detect more general features than drastically overfitting
   * the dataset.
   * 
   * @param rnd the random number generator to consult.
   * @param activations activations of units per record on each column.
   * @param p dropout probability.
   */
  public static void dropout(Random rnd, DoubleMatrix activations, double p) {
    for (int row = 0; row < activations.getRowCount(); row++) {
      for (int col = 0; col < activations.getColumnCount(); col++) {
        if (rnd.nextDouble() <= p) {
          activations.set(row, col, 0d);
        }
      }
    }
  }

}
