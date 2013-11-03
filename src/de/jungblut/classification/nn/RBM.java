package de.jungblut.classification.nn;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.jungblut.math.DoubleMatrix;
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
 * Class for training and stacking Restricted Boltzmann Machines (RBMs). Stacked
 * RBMs are called DBN (deep belief net). Usually every layer of a deep belief
 * net is training greedily with the contrastive divergence algorithm
 * implemented in {@link RBMCostFunction}. Create new instances with the
 * {@link RBMBuilder} or with the static factory methods.
 * 
 * @author thomas.jungblut
 * 
 */
public final class RBM {

  private static final Log LOG = LogFactory.getLog(RBM.class);

  public static class RBMBuilder {

    private final int[] layerSizes;
    private final ActivationFunction function;

    private TrainingType type = TrainingType.CPU;
    private double lambda;
    private boolean verbose = false;
    private boolean stochastic = false;
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
     * Sets the training mode to stochastic.
     */
    public RBMBuilder stochastic() {
      return stochastic(true);
    }

    /**
     * If verbose is true, stochastic training will be used.
     */
    public RBMBuilder stochastic(boolean stochastic) {
      this.stochastic = stochastic;
      return this;
    }

    /**
     * If verbose is true, progress indicators will be printed to STDOUT.
     */
    public RBMBuilder verbose(boolean verbose) {
      this.verbose = verbose;
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
  private final DoubleMatrix[] weights;
  private final ActivationFunction activationFunction;

  private TrainingType type = TrainingType.CPU;
  private double lambda;

  private boolean stochastic;
  private boolean verbose;
  // if zero, the complete batch learning will be used
  private int miniBatchSize = 0;
  // default is a single thread
  private int batchParallelism = 1;

  private long seed;

  // serialization constructor
  private RBM(int[] stackedHiddenLayerSizes,
      ActivationFunction activationFunction, TrainingType type) {
    this.layerSizes = stackedHiddenLayerSizes;
    this.activationFunction = activationFunction;
    this.weights = new DenseDoubleMatrix[layerSizes.length];
    this.type = type;
    seed = System.currentTimeMillis();
  }

  private RBM(RBMBuilder rbmBuilder) {
    this(rbmBuilder.layerSizes, rbmBuilder.function, rbmBuilder.type);
    this.lambda = rbmBuilder.lambda;
    this.verbose = rbmBuilder.verbose;
    this.miniBatchSize = rbmBuilder.miniBatchSize;
    this.batchParallelism = rbmBuilder.batchParallelism;
    this.stochastic = rbmBuilder.stochastic;
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
   * @param currentTrainingSet the training set to train on. This trainingset
   *          will be mutated and changed during the training, so make sure you
   *          make a defensive copy if you need the examples later on.
   * @param minimizer the minimizer to use. Note that the costfunction's
   *          gradient isn't the real gradient and thus can't be optimized by
   *          line searching minimizers like {@link Fmincg}.
   * @param numIterations how many iterations of training have to be done. (if
   *          converged before, it will stop training)
   */
  public void train(DoubleVector[] currentTrainingSet, Minimizer minimizer,
      int numIterations) {
    // start with greedy layerwise training
    for (int i = 0; i < layerSizes.length; i++) {
      if (verbose) {
        LOG.info("Training stack at height: " + i);
      }
      // add the bias to hidden and visible layer, random init with 0.1*randn
      DenseDoubleMatrix start = new DenseDoubleMatrix(layerSizes[i] + 1,
          currentTrainingSet[0].getDimension() + 1, new Random(seed))
          .multiply(0.1d);
      DoubleVector folded = DenseMatrixFolder.foldMatrices(start);
      start = null;
      // now do the real training
      RBMCostFunction fnc = new RBMCostFunction(currentTrainingSet,
          miniBatchSize, batchParallelism, layerSizes[i], activationFunction,
          type, lambda, seed, stochastic);
      DoubleVector theta = minimizer.minimize(fnc, folded, numIterations,
          verbose);
      // get back our weights as a matrix
      DoubleMatrix thetaMat = DenseMatrixFolder.unfoldMatrices(theta,
          fnc.getUnfoldParameters())[0];
      weights[i] = thetaMat;
      // now we can get our new training set for the next stack
      if (i + 1 != layerSizes.length) {
        for (int row = 0; row < currentTrainingSet.length; row++) {
          currentTrainingSet[row] = computeHiddenActivations(
              currentTrainingSet[row], weights[i]);
          // slice the old bias off
          currentTrainingSet[row] = currentTrainingSet[row].slice(1,
              currentTrainingSet[row].getDimension());
        }
      }
    }
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
      lastOutput = computeHiddenActivations(lastOutput, weights[i]);
    }
    // slice the hidden bias away
    return lastOutput.slice(1, lastOutput.getDimension());
  }

  /**
   * Creates a reconstruction of the input using the given hidden activations.
   * (That, what is returned by {@link #predict(DoubleVector)}).
   * 
   * @param hiddenActivations the activations of the predict method.
   * @return the reconstructed input vector.
   */
  public DoubleVector reconstructInput(DoubleVector hiddenActivations) {
    DoubleVector lastOutput = hiddenActivations;
    for (int i = weights.length - 1; i >= 0; i--) {
      lastOutput = computeHiddenActivations(lastOutput, weights[i].transpose());
    }
    // slice the hidden bias away
    return lastOutput.slice(1, lastOutput.getDimension());
  }

  /**
   * @return the weight matrices.
   */
  public DoubleMatrix[] getWeights() {
    return this.weights;
  }

  /**
   * Creates a weight matrix that can be used for unsupervised weight
   * initialization in the {@link MultilayerPerceptron}.
   * 
   * @param outputLayerSize the size of the classification layer on top of this
   *          RBM.
   * @return the {@link WeightMatrix} that maps layers to the weights.
   */
  public WeightMatrix[] getNeuralNetworkWeights(int outputLayerSize) {
    WeightMatrix[] toReturn = new WeightMatrix[this.weights.length + 1];

    // translate the matrices
    for (int i = 0; i < weights.length; i++) {
      toReturn[i] = new WeightMatrix(this.weights[i].slice(1,
          weights[i].getRowCount(), 0, weights[i].getColumnCount()));
    }
    // add a last layer on top of it
    toReturn[toReturn.length - 1] = new WeightMatrix(
        toReturn[toReturn.length - 2].getWeights().getRowCount(),
        outputLayerSize);
    return toReturn;
  }

  /**
   * Sets the internally used rng seed.
   */
  public void setSeed(long seed) {
    this.seed = seed;
  }

  private DoubleVector computeHiddenActivations(DoubleVector input,
      DoubleMatrix theta) {
    // add the bias to the input
    DoubleVector biased = new DenseDoubleVector(1d, input.toArray());
    return activationFunction.apply(theta.multiplyVectorRow(biased));
  }

  /**
   * Serializes this RBM model into the given output stream.
   */
  public static void serialize(RBM model, DataOutput out) throws IOException {
    out.writeInt(model.layerSizes.length);
    for (int layer : model.layerSizes) {
      out.writeInt(layer);
    }

    for (DoubleMatrix mat : model.weights) {
      new MatrixWritable(mat).write(out);
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

    DoubleMatrix[] array = new DoubleMatrix[layers];
    for (int i = 0; i < layers; i++) {
      MatrixWritable mv = new MatrixWritable();
      mv.readFields(in);
      array[i] = mv.getMatrix();
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
