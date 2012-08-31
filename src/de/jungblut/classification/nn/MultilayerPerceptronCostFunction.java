package de.jungblut.classification.nn;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.tuple.Tuple;

/**
 * Neural network costfunction for a multilayer perceptron.
 * 
 */
public class MultilayerPerceptronCostFunction implements CostFunction {

  private final DenseDoubleMatrix x;
  private final DenseDoubleMatrix y;
  private final double lambda;

  private final int m;
  private final int[] layerSizes;
  private final int[][] unfoldParameters;

  public MultilayerPerceptronCostFunction(MultilayerPerceptron network,
      DenseDoubleMatrix x, DenseDoubleMatrix y, double lambda) {
    this.m = x.getRowCount();
    this.x = new DenseDoubleMatrix(DenseDoubleVector.ones(m), x);
    this.y = y;
    this.lambda = lambda;
    this.layerSizes = new int[network.getLayers().length];
    for (int i = 0; i < layerSizes.length; i++) {
      layerSizes[i] = network.getLayers()[i].getLength();
    }
    this.unfoldParameters = computeUnfoldParameters(layerSizes);
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
    ax[0] = x;
    for (int i = 1; i < layerSizes.length; i++) {
      zx[i] = ax[i - 1].multiply(thetas[i - 1].transpose());

      if (i < (layerSizes.length - 1)) {
        ax[i] = new DenseDoubleMatrix(DenseDoubleVector.ones(m),
            sigmoidMatrix((DenseDoubleMatrix) zx[i]));
      } else {
        // the output doesn't need a bias
        ax[i] = sigmoidMatrix((DenseDoubleMatrix) zx[i]);
      }
    }

    // only calculate the regularization term if lambda is not 0
    double regularization = 0.0d;
    if (lambda != 0.0d) {
      for (int i = 0; i < thetas.length; i++) {
        regularization += (thetas[i].slice(0, thetas[i].getRowCount(), 1,
            thetas[i].getColumnCount())).pow(2).sum();
      }
      regularization = (lambda / (2.0d * m)) * regularization;
    }

    // now backpropagate the error backwards by calculating the deltas.
    // also here we are following the math equations and nulling out the 0th
    // entry.
    DoubleMatrix[] deltaX = new DoubleMatrix[layerSizes.length];
    // statically set the last delta to the difference of outcome and prediction
    deltaX[deltaX.length - 1] = ax[layerSizes.length - 1].subtract(y);
    // compute the deltas onto the input layer
    for (int i = (layerSizes.length - 2); i > 0; i--) {
      deltaX[i] = deltaX[i + 1].multiply(
          thetas[i].slice(0, thetas[i].getRowCount(), 1,
              thetas[i].getColumnCount())).multiplyElementWise(
          sigmoidGradientMatrix((DenseDoubleMatrix) zx[i]));
    }

    // calculate our gradients
    for (int i = 0; i < thetaGradients.length; i++) {
      thetaGradients[i] = (DenseDoubleMatrix) (deltaX[i + 1].transpose()
          .multiply(ax[i]).multiply(1.0d / m)).add((thetas[i].multiply(lambda
          / m)));
      // subtract the regularized bias
      thetaGradients[i].setColumnVector(0,
          thetas[i].slice(0, thetas[i].getRowCount(), 0, 1)
              .multiply(lambda / m).getColumnVector(0));
    }

    // calculate our cost function (error in the last layer)
    double j = (1.0d / m)
        * (y.multiply(-1).multiplyElementWise(
            logMatrix(ax[layerSizes.length - 1])).subtract((y.subtractBy(1.0d))
            .multiplyElementWise(logMatrix(ax[layerSizes.length - 1]
                .subtractBy(1.0d))))).sum() + regularization;

    return new Tuple<Double, DoubleVector>(j,
        DenseMatrixFolder.foldMatrices(thetaGradients));
  }

  /*
   * some static helpers for sigmoid and log calculation
   */

  static double sigmoid(double input) {
    return 1.0 / (1.0 + Math.exp(-input));
  }

  static double sigmoidGradient(double input) {
    return sigmoid(input) * (1 - sigmoid(input));
  }

  static DenseDoubleMatrix sigmoidMatrix(DenseDoubleMatrix input) {
    DenseDoubleMatrix sigmoid = DenseDoubleMatrix.copy(input);
    for (int row = 0; row < sigmoid.getRowCount(); row++) {
      for (int col = 0; col < sigmoid.getColumnCount(); col++) {
        sigmoid.set(row, col, sigmoid(sigmoid.get(row, col)));
      }
    }
    return sigmoid;
  }

  static DenseDoubleMatrix sigmoidGradientMatrix(DenseDoubleMatrix input) {
    DenseDoubleMatrix sigmoid = DenseDoubleMatrix.copy(input);
    for (int row = 0; row < sigmoid.getRowCount(); row++) {
      for (int col = 0; col < sigmoid.getColumnCount(); col++) {
        sigmoid.set(row, col, sigmoidGradient(sigmoid.get(row, col)));
      }
    }
    return sigmoid;
  }

  static DenseDoubleMatrix logMatrix(DenseDoubleMatrix input) {
    DenseDoubleMatrix log = DenseDoubleMatrix.copy(input);
    for (int row = 0; row < log.getRowCount(); row++) {
      for (int col = 0; col < log.getColumnCount(); col++) {
        log.set(row, col, Math.log(log.get(row, col)));
      }
    }
    return log;
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
