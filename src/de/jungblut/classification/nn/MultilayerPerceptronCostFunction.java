package de.jungblut.classification.nn;

import java.util.Random;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.tuple.Tuple;

/**
 * Neural network costfunction for a multilayer perceptron.
 * 
 * @author thomas.jungblut
 */
public class MultilayerPerceptronCostFunction implements CostFunction {

  private final DenseDoubleMatrix x;
  private final DenseDoubleMatrix y;
  private final double lambda;

  private final int m;
  private final int[] layerSizes;
  private final int[][] unfoldParameters;

  private final ActivationFunction[] activations;
  private final ErrorFunction error;

  private final DenseDoubleVector ones;

  private final double visibleDropoutProbability;
  private final double hiddenDropoutProbability;
  private final Random rnd;

  public MultilayerPerceptronCostFunction(MultilayerPerceptron network,
      DenseDoubleMatrix x, DenseDoubleMatrix y, double lambda) {
    this.m = x.getRowCount();
    this.ones = DenseDoubleVector.ones(m);
    this.x = new DenseDoubleMatrix(ones, x);
    this.y = y;
    this.lambda = lambda;
    this.layerSizes = network.getLayers();
    this.unfoldParameters = computeUnfoldParameters(layerSizes);
    this.activations = network.getActivations();
    this.error = network.getError();
    this.visibleDropoutProbability = network.getVisibleDropoutProbability();
    this.hiddenDropoutProbability = network.getHiddenDropoutProbability();
    this.rnd = new Random();
  }

  /**
   * Input contains the network parameters (weights) as a folded vector. Code
   * mainly taken from ml-class to work with the fmincg to optimize the theta
   * weights. I have generified it to a solution to work with multiple hidden
   * layers.
   */
  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

    DenseDoubleMatrix[] thetas = DenseMatrixFolder.unfoldMatrices(input,
        unfoldParameters);
    DenseDoubleMatrix[] thetaGradients = new DenseDoubleMatrix[thetas.length];

    // start forward propagation
    // we compute the aX activations for all layers
    DenseDoubleMatrix[] ax = new DenseDoubleMatrix[layerSizes.length];
    // for zX we constantly null the zero index, since it has no use
    DoubleMatrix[] zx = new DoubleMatrix[layerSizes.length];

    // for the first weights, we don't need to compute Z
    if (visibleDropoutProbability > 0d) {
      // compute dropout for ax[0], copy X to not alter internal
      // representation
      ax[0] = DenseDoubleMatrix.copy(x);
      dropout(ax[0], visibleDropoutProbability);
    } else {
      ax[0] = x;
    }
    for (int i = 1; i < layerSizes.length; i++) {
      zx[i] = multiply(ax[i - 1], thetas[i - 1], false, true);

      if (i < (layerSizes.length - 1)) {
        ax[i] = new DenseDoubleMatrix(ones, activations[i].apply(zx[i]));
        if (hiddenDropoutProbability > 0d) {
          // compute dropout for ax[i]
          dropout(ax[i], hiddenDropoutProbability);
        }
      } else {
        // the output doesn't need a bias
        ax[i] = (DenseDoubleMatrix) activations[i].apply(zx[i]);
      }
    }

    // only calculate the regularization term if lambda is not 0
    double regularization = 0.0d;
    if (lambda != 0d) {
      for (DenseDoubleMatrix theta : thetas) {
        regularization += (theta.slice(0, theta.getRowCount(), 1,
            theta.getColumnCount())).pow(2).sum();
      }
      regularization = (lambda / (2.0d * m)) * regularization;
    }

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

    // calculate the gradients of the weights
    for (int i = 0; i < thetaGradients.length; i++) {
      DoubleMatrix gradDXA = multiply(deltaX[i + 1], ax[i], true, false);
      thetaGradients[i] = (DenseDoubleMatrix) (gradDXA.multiply(1.0d / m));
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

    // calculate our cost function (error in the last layer)
    double j = (1.0d / m) * error.getError(y, ax[layerSizes.length - 1])
        + regularization;

    return new Tuple<Double, DoubleVector>(j,
        DenseMatrixFolder.foldMatrices(thetaGradients));
  }

  /**
   * Computes dropout for the activations matrix. Each element for each row has
   * the similar probability p to be "dropped out" (set to 0) of the
   * computation. This way, the network does learn to not rely on other units
   * thus learning to detect more general features than drastically overfitting
   * the dataset.
   * 
   * @param activations activations of units per record on each column.
   * @param p dropout probability.
   */
  private void dropout(DenseDoubleMatrix activations, double p) {
    for (int row = 0; row < activations.getRowCount(); row++) {
      for (int col = 0; col < activations.getColumnCount(); col++) {
        if (rnd.nextDouble() <= p) {
          activations.set(row, col, 0d);
        }
      }
    }
  }

  /**
   * General matrix multiplication of two matrices
   * 
   * @param ax the activation matrix.
   * @param theta the weight matrix.
   * @param axTranspose
   * @param thetaTranspose
   * @return the matrix that contains the result of the multiplication of both
   *         parameters.
   */
  protected DoubleMatrix multiply(DoubleMatrix a1, DoubleMatrix a2,
      boolean a1Transpose, boolean a2Transpose) {
    a2 = a2Transpose ? a2.transpose() : a2;
    a1 = a1Transpose ? a1.transpose() : a1;
    return a1.multiply(a2);
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

}
