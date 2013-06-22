package de.jungblut.classification.nn;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Random;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.activation.ActivationFunctionSelector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.minimize.GradientDescent;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.writable.MatrixWritable;

/**
 * Class for training/stacking RBMs. Create new instances with the
 * {@link RBMBuilder}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class RBM {

  // test seed
  static long SEED = System.currentTimeMillis();

  public static class RBMBuilder {

    private final int[] layerSizes;
    private final ActivationFunction function;

    private TrainingType type = TrainingType.CPU;
    private double lambda;
    private double hiddenDropoutProbability;
    private double visibleDropoutProbability;
    private boolean verbose = false;
    private int miniBatchSize;
    private int batchParallelism = Runtime.getRuntime().availableProcessors();

    private RBMBuilder(int[] layer, ActivationFunction activation) {
      this.layerSizes = layer;
      this.function = activation;
    }

    /**
     * Sets the training type, it defaults to CPU- so only use if you want to
     * use the GPU.
     */
    public RBMBuilder trainingType(TrainingType type) {
      this.type = type;
      return this;
    }

    /**
     * Sets the regularization parameter lambda, defaults to 0 if not set.
     */
    public RBMBuilder lambda(double lambda) {
      this.lambda = lambda;
      return this;
    }

    /**
     * @param size the minibatch size to use. Batches are calculated in parallel
     *          on every cpu core if not overridden by
     *          {@link #batchParallelism(int)}.
     */
    public RBMBuilder miniBatchSize(int size) {
      this.miniBatchSize = size;
      return this;
    }

    /**
     * @param numThreads set the number of threads where batches should be
     *          calculated in parallel.
     */
    public RBMBuilder batchParallelism(int numThreads) {
      this.batchParallelism = numThreads;
      return this;
    }

    /**
     * Sets verbose to true. Progress indicators will be printed to STDOUT.
     */
    public RBMBuilder verbose() {
      return verbose(true);
    }

    /**
     * If verbose is true, progress indicators will be printed to STDOUT.
     */
    public RBMBuilder verbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    /**
     * Sets the hidden layer dropout probability.
     */
    public RBMBuilder hiddenLayerDropout(double d) {
      this.hiddenDropoutProbability = d;
      return this;
    }

    /**
     * Sets the input layer dropout probability.
     */
    public RBMBuilder inputLayerDropout(double d) {
      this.visibleDropoutProbability = d;
      return this;
    }

    /**
     * Creates a new {@link RBMBuilder} from an activation function and
     * layersizes.
     * 
     * @param activation the activation function.
     * @param layer an array of hidden layer sizes.
     * @return a new RBM builder.
     */
    public static RBMBuilder create(ActivationFunction activation, int... layer) {
      return new RBMBuilder(layer, activation);
    }

    /**
     * @return a new {@link RBM} with the given configuration.
     */
    public RBM build() {
      return new RBM(this);
    }

  }

  private final int[] layerSizes;
  private final DenseDoubleMatrix[] weights;
  private final ActivationFunction activationFunction;
  private final Random random;

  private TrainingType type = TrainingType.CPU;
  private double lambda;
  private double hiddenDropoutProbability;
  private double visibleDropoutProbability;
  private boolean verbose = false;
  // if zero, the complete batch learning will be used
  private int miniBatchSize = 0;
  // default is a single thread
  private int batchParallelism = 1;

  // serialization constructor
  private RBM(int[] stackedHiddenLayerSizes,
      ActivationFunction activationFunction, TrainingType type) {
    this.layerSizes = stackedHiddenLayerSizes;
    this.activationFunction = activationFunction;
    this.weights = new DenseDoubleMatrix[layerSizes.length];
    this.type = type;
    random = new Random(SEED);
  }

  private RBM(RBMBuilder rbmBuilder) {
    this(rbmBuilder.layerSizes, rbmBuilder.function, rbmBuilder.type);
    this.lambda = rbmBuilder.lambda;
    this.hiddenDropoutProbability = rbmBuilder.hiddenDropoutProbability;
    this.visibleDropoutProbability = rbmBuilder.visibleDropoutProbability;
    this.verbose = rbmBuilder.verbose;
    this.miniBatchSize = rbmBuilder.miniBatchSize;
    this.batchParallelism = rbmBuilder.batchParallelism;
  }

  /**
   * Trains the RBM on the given training set.
   * 
   * @param trainingSet the training set to train on.
   * @param alpha the learning rate for gradient descent.
   * @param numIterations how many iterations of training have to be done. (if
   *          converged before, it will stop training)
   */
  public void train(DoubleVector[] trainingSet, double alpha, int numIterations) {
    train(trainingSet, new GradientDescent(alpha, 0d), numIterations);
  }

  /**
   * Trains the RBM on the given training set.
   * 
   * @param trainingSet the training set to train on.
   * @param minimizer the minimizer to use. Note that the costfunction's
   *          gradient isn't the real gradient and thus can't be optimized by
   *          line searching minimizers like {@link Fmincg}.
   * @param numIterations how many iterations of training have to be done. (if
   *          converged before, it will stop training)
   */
  public void train(DoubleVector[] trainingSet, Minimizer minimizer,
      int numIterations) {
    DoubleVector[] tmpTrainingSet = null;
    for (int i = 0; i < layerSizes.length; i++) {
      if (verbose) {
        System.out.println("Training stack at height: " + i);
      }
      DoubleVector[] currentTrainingSet = i == 0 ? trainingSet : tmpTrainingSet;
      // unfolded contains the bias
      WeightMatrix unfolded = new WeightMatrix(
          currentTrainingSet[0].getDimension(), layerSizes[i]);
      DenseDoubleVector folded = DenseMatrixFolder.foldMatrices(unfolded
          .getWeights());
      // now do the real training
      RBMCostFunction fnc = new RBMCostFunction(currentTrainingSet,
          miniBatchSize, batchParallelism, layerSizes[i], activationFunction,
          type, lambda, visibleDropoutProbability, hiddenDropoutProbability);
      DoubleVector theta = minimizer.minimize(fnc, folded, numIterations,
          verbose);
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
      RBMCostFunction.binarize(random, hiddenProbability);
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

    DenseDoubleMatrix[] array = new DenseDoubleMatrix[layers];
    for (int i = 0; i < layers; i++) {
      array[i] = MatrixWritable.readDenseMatrix(in);
    }
    ActivationFunction func = null;
    try {
      func = (ActivationFunction) Class.forName(in.readUTF()).newInstance();
    } catch (InstantiationException | IllegalAccessException
        | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    RBM model = new RBM(sizes, func, TrainingType.CPU);
    for (int i = 0; i < layers; i++) {
      model.weights[i] = array[i];
    }
    return model;
  }

  /*
   * some helper static factories other than the builder pattern.
   */

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
