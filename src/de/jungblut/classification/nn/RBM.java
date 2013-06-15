package de.jungblut.classification.nn;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.activation.ActivationFunctionSelector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.GradientDescent;
import de.jungblut.writable.MatrixWritable;

/**
 * Class for training/stacking RBMs.
 * 
 * @author thomas.jungblut
 * 
 */
public final class RBM {

  private final int[] layerSizes;
  private final DenseDoubleMatrix[] weights;
  private ActivationFunction activationFunction;
  private TrainingType type = TrainingType.CPU;

  private RBM(int[] stackedHiddenLayerSizes,
      ActivationFunction activationFunction, TrainingType type) {
    this.layerSizes = stackedHiddenLayerSizes;
    this.activationFunction = activationFunction;
    this.weights = new DenseDoubleMatrix[layerSizes.length];
    this.type = type;
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
      RBMCostFunction fnc = new RBMCostFunction(mat, layerSizes[i],
          activationFunction, type);
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
      lastOutput = computeHiddenActivations(lastOutput, weights[i],
          !(i + 1 == layerSizes.length));
    }
    return lastOutput;
  }

  /**
   * @return the weight matrices.
   */
  public DenseDoubleMatrix[] getWeights() {
    return this.weights;
  }

  private DoubleVector computeHiddenActivations(DoubleVector input,
      DenseDoubleMatrix theta, boolean binarize) {
    // add the bias to the input
    DoubleVector biased = new DenseDoubleVector(1d, input.toArray());
    DoubleVector hiddenProbability = activationFunction.apply(theta
        .multiplyVectorRow(biased));
    // now binarize with the contained probability
    if (binarize) {
      RBMCostFunction.binarize(hiddenProbability);
    }
    return hiddenProbability;
  }

  /**
   * Serializes this RBM model into the given output stream.
   */
  public static void serialize(RBM model, DataOutput out) throws IOException {
    out.writeInt(model.layerSizes.length);
    for (int layer : model.layerSizes) {
      out.writeInt(layer);
    }

    for (DenseDoubleMatrix mat : model.weights) {
      MatrixWritable.writeDenseMatrix(mat, out);
    }

    out.writeUTF(model.activationFunction.getClass().getName());

  }

  /**
   * Deserializes the RBM back from the binary stream input.
   */
  public static RBM deserialize(DataInputStream in) throws IOException {
    int layers = in.readInt();
    int[] sizes = new int[layers];
    for (int i = 0; i < layers; i++) {
      sizes[i] = in.readInt();
    }

    RBM model = stacked(sizes);
    for (int i = 0; i < layers; i++) {
      model.weights[i] = MatrixWritable.readDenseMatrix(in);
    }
    try {
      model.activationFunction = (ActivationFunction) Class.forName(
          in.readUTF()).newInstance();
    } catch (InstantiationException | IllegalAccessException
        | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return model;
  }

  /**
   * @return a single RBM which isn't stacked and emits to the given number of
   *         hidden nodes.
   */
  public static RBM single(int numHiddenNodes, ActivationFunction func) {
    return new RBM(new int[] { numHiddenNodes }, func, TrainingType.CPU);
  }

  /**
   * Creates a new stacked RBM with sigmoid activation and with the given number
   * of hidden nodes in each stacked layer. For example: 4,3,2 will create the
   * first RBM with 4 hidden nodes, the second layer will operate on the 4
   * hidden node outputs of the RBM before and emit to 3 hidden nodes. Similarly
   * the last layer will receive three inputs and emit 2 output's, which state
   * you receive in the predict method.
   */
  public static RBM stacked(ActivationFunction func, int... numHiddenNodes) {
    return new RBM(numHiddenNodes, func, TrainingType.CPU);
  }

  /**
   * @return a single RBM with sigmoid activation which isn't stacked and emits
   *         to the given number of hidden nodes.
   */
  public static RBM single(int numHiddenNodes) {
    return new RBM(new int[] { numHiddenNodes },
        ActivationFunctionSelector.SIGMOID.get(), TrainingType.CPU);
  }

  /**
   * Creates a new stacked RBM with sigmoid activation and with the given number
   * of hidden nodes in each stacked layer. For example: 4,3,2 will create the
   * first RBM with 4 hidden nodes, the second layer will operate on the 4
   * hidden node outputs of the RBM before and emit to 3 hidden nodes. Similarly
   * the last layer will receive three inputs and emit 2 output's, which state
   * you receive in the predict method.
   */
  public static RBM stacked(int... numHiddenNodes) {
    return new RBM(numHiddenNodes, ActivationFunctionSelector.SIGMOID.get(),
        TrainingType.CPU);
  }

  /**
   * @return a single RBM which isn't stacked and emits to the given number of
   *         hidden nodes.
   */
  public static RBM singleGPU(int numHiddenNodes, ActivationFunction func) {
    return new RBM(new int[] { numHiddenNodes }, func, TrainingType.GPU);
  }

  /**
   * Creates a new stacked RBM with sigmoid activation and with the given number
   * of hidden nodes in each stacked layer. For example: 4,3,2 will create the
   * first RBM with 4 hidden nodes, the second layer will operate on the 4
   * hidden node outputs of the RBM before and emit to 3 hidden nodes. Similarly
   * the last layer will receive three inputs and emit 2 output's, which state
   * you receive in the predict method.
   */
  public static RBM stackedGPU(ActivationFunction func, int... numHiddenNodes) {
    return new RBM(numHiddenNodes, func, TrainingType.GPU);
  }

}
