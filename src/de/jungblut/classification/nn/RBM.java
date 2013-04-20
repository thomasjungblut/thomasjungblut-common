package de.jungblut.classification.nn;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.activation.ActivationFunctionSelector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.GradientDescent;

/**
 * Class for training/stacking RBMs.
 * 
 * @author thomas.jungblut
 * 
 */
public final class RBM {

  private static final ActivationFunction SIGMOID = ActivationFunctionSelector.SIGMOID
      .get();

  private final int[] layerSizes;
  private final DenseDoubleMatrix[] weights;

  private RBM(int[] stackedHiddenLayerSizes) {
    this.layerSizes = stackedHiddenLayerSizes;
    this.weights = new DenseDoubleMatrix[layerSizes.length];
  }

  /**
   * Trains the RBM on the given training set.
   * 
   * @param trainingSet the training set to train on.
   * @param learningRate the learning rate to use.
   * @param numIterations how many iterations of training have to be done. (if
   *          converged before, it will stop training)
   * @param verbose if true, output to STDOUT containing progress and iteration
   *          costs.
   */
  public void train(DoubleVector[] trainingSet, double learningRate,
      int numIterations, boolean verbose) {
    DoubleVector[] tmpTrainingSet = null;
    for (int i = 0; i < layerSizes.length; i++) {
      if (verbose) {
        System.out.println("Training stack at height: " + i);
      }
      DoubleVector[] currentTrainingSet = i == 0 ? trainingSet : tmpTrainingSet;
      DenseDoubleMatrix mat = new DenseDoubleMatrix(currentTrainingSet);
      // unfolded contains the bias
      WeightMatrix unfolded = new WeightMatrix(mat.getColumnCount(),
          layerSizes[i]);
      DenseDoubleVector folded = DenseMatrixFolder.foldMatrices(unfolded
          .getWeights());
      // now do the real training
      RBMCostFunction fnc = new RBMCostFunction(mat, layerSizes[i]);
      DoubleVector theta = GradientDescent.minimizeFunction(fnc, folded,
          learningRate, 0d, numIterations, verbose);
      // get back our weights as a matrix
      DenseDoubleMatrix thetaMat = DenseMatrixFolder.unfoldMatrices(theta,
          fnc.getUnfoldParameters())[0];
      weights[i] = thetaMat;
      // now we can get our new training set for the next stack
      if (i + 1 != layerSizes.length) {
        if (tmpTrainingSet == null) {
          tmpTrainingSet = new DoubleVector[trainingSet.length];
        }
        for (int row = 0; row < currentTrainingSet.length; row++) {
          // we binarize between the layers
          tmpTrainingSet[row] = computeHiddenActivations(
              currentTrainingSet[row], weights[i], true);
        }

      }
    }
  }

  /**
   * Returns the hidden activations of the last RBM.
   * 
   * @param input the input of the first RBM.
   * @return a vector that contains the values (0 or 1) of the hidden
   *         activations on the last layer.
   */
  public DoubleVector predictBinary(DoubleVector input) {
    DoubleVector lastOutput = input;
    for (int i = 0; i < layerSizes.length; i++) {
      lastOutput = computeHiddenActivations(lastOutput, weights[i], true);
    }
    return lastOutput;
  }

  /**
   * Returns the hidden activations of the last RBM.
   * 
   * @param input the input of the first RBM.
   * @return a vector that contains the values of the hidden activations on the
   *         last layer.
   */
  public DoubleVector predict(DoubleVector input) {
    DoubleVector lastOutput = input;
    for (int i = 0; i < layerSizes.length; i++) {
      lastOutput = computeHiddenActivations(lastOutput, weights[i], false);
    }
    return lastOutput;
  }

  private DoubleVector computeHiddenActivations(DoubleVector input,
      DenseDoubleMatrix theta, boolean binarize) {
    // add the bias to the input
    DoubleVector biased = new DenseDoubleVector(1d, input.toArray());
    DoubleVector hiddenProbability = SIGMOID
        .apply(theta.multiplyVector(biased));
    // now binarize with the contained probability
    if (binarize) {
      RBMCostFunction.binarize(hiddenProbability);
    }
    return hiddenProbability;
  }

  /**
   * @return a single RBM which isn't stacked and emits to the given number of
   *         hidden nodes.
   */
  public static RBM single(int numHiddenNodes) {
    return new RBM(new int[] { numHiddenNodes });
  }

  /**
   * Creates a new stacked RBM with the given number of hidden nodes in each
   * stacked layer. For example: 4,3,2 will create the first RBM with 4 hidden
   * nodes, the second layer will operate on the 4 hidden node outputs of the
   * RBM before and emit to 3 hidden nodes. Similarly the last layer will
   * receive three inputs and emit 2 output's, which state you receive in the
   * predict method.
   */
  public static RBM stacked(int... numHiddenNodes) {
    return new RBM(numHiddenNodes);
  }

}
