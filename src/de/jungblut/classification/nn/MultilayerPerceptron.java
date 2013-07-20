package de.jungblut.classification.nn;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.google.common.base.Preconditions;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.classification.Classifier;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.activation.LinearActivationFunction;
import de.jungblut.math.activation.SigmoidActivationFunction;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.math.squashing.ErrorFunction;
import de.jungblut.writable.MatrixWritable;

/**
 * Multilayer perceptron implementation that works on GPU via JCuda and CPU. It
 * features l0/1 regularization (lambda) and dropout. A wide variety of
 * activation functions and error functions can be configured. You can set this
 * network up by using the builder given by {@link MultilayerPerceptron}.
 * {@link MultilayerPerceptronBuilder}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MultilayerPerceptron extends AbstractClassifier {

  public static long SEED = System.currentTimeMillis();

  /**
   * Configuration for training a neural net through the {@link Classifier}
   * 
   */
  public static final class MultilayerPerceptronBuilder {
    private final Minimizer minimizer;
    private final int maxIterations;
    private final int[] layer;
    private final ActivationFunction[] activationFunctions;
    private final ErrorFunction error;

    private TrainingType type = TrainingType.CPU;
    private double lambda = 0d;
    private boolean verbose = false;
    private double hiddenDropoutProbability = 0d;
    private double visibleDropoutProbability = 0d;
    private WeightMatrix[] weights;

    private boolean stochastic = false;
    private int miniBatchSize;
    private int batchParallelism = Runtime.getRuntime().availableProcessors();

    private MultilayerPerceptronBuilder(int[] layer,
        ActivationFunction[] activations, Minimizer minimizer,
        int maxIterations, ErrorFunction error) {
      this.layer = layer;
      this.minimizer = minimizer;
      this.error = error;
      this.maxIterations = maxIterations;
      this.activationFunctions = activations;
    }

    /**
     * Sets the training type, it defaults to CPU- so only use if you want to
     * use the GPU.
     */
    public MultilayerPerceptronBuilder trainingType(TrainingType type) {
      this.type = type;
      return this;
    }

    /**
     * Sets the regularization parameter lambda, defaults to 0 if not set.
     */
    public MultilayerPerceptronBuilder lambda(double lambda) {
      this.lambda = lambda;
      return this;
    }

    /**
     * Sets verbose to true. Progress indicators will be printed to STDOUT.
     */
    public MultilayerPerceptronBuilder verbose() {
      return verbose(true);
    }

    /**
     * If verbose is true, progress indicators will be printed to STDOUT.
     */
    public MultilayerPerceptronBuilder verbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    /**
     * Sets the hidden layer dropout probability.
     */
    public MultilayerPerceptronBuilder hiddenLayerDropout(double d) {
      this.hiddenDropoutProbability = d;
      return this;
    }

    /**
     * Sets the input layer dropout probability.
     */
    public MultilayerPerceptronBuilder inputLayerDropout(double d) {
      this.visibleDropoutProbability = d;
      return this;
    }

    /**
     * Sets the initial weights, maybe from an already trained network, or from
     * a fancy random initialization technique.
     */
    public MultilayerPerceptronBuilder withWeights(WeightMatrix[] weights) {
      this.weights = weights;
      return this;
    }

    /**
     * @param size the minibatch size to use. Batches are calculated in parallel
     *          on every cpu core if not overridden by
     *          {@link #batchParallelism(int)}.
     */
    public MultilayerPerceptronBuilder miniBatchSize(int size) {
      this.miniBatchSize = size;
      return this;
    }

    /**
     * @param numThreads set the number of threads where batches should be
     *          calculated in parallel.
     */
    public MultilayerPerceptronBuilder batchParallelism(int numThreads) {
      this.batchParallelism = numThreads;
      return this;
    }

    /**
     * Sets the training mode to stochastic.
     */
    public MultilayerPerceptronBuilder stochastic() {
      return stochastic(true);
    }

    /**
     * If verbose is true, stochastic training will be used.
     */
    public MultilayerPerceptronBuilder stochastic(boolean stochastic) {
      this.stochastic = stochastic;
      return this;
    }

    /**
     * @return a new {@link MultilayerPerceptron} with the given configuration.
     */
    public MultilayerPerceptron build() {
      return new MultilayerPerceptron(this);
    }

    /**
     * Creates a new TrainingConfiguration with the mandatory configurations of
     * the activation functions, the to be used minimizer and the maximum
     * iterations.
     * 
     * @param layer the number of neurons for each layer, each index denotes a
     *          layer.
     * @param activations the activation functions to be used, each index
     *          denotes a layer.
     * @param errorFunction the error function on the last layer.
     * @param minimizer the minimizer to be used.
     * @param maxIterations how many iterations (epochs) to run.
     * @return a brand new training configuration with the given parameters set.
     */
    public static MultilayerPerceptronBuilder create(int[] layer,
        ActivationFunction[] activations, ErrorFunction errorFunction,
        Minimizer minimizer, int maxIteration) {
      return new MultilayerPerceptronBuilder(layer, activations, minimizer,
          maxIteration, errorFunction);
    }

  }

  private final WeightMatrix[] weights;
  private final Minimizer minimizer;
  private final int maxIterations;
  private final int[] layers;
  private final ActivationFunction[] activations;

  private double lambda;
  private double hiddenDropoutProbability;
  private double visibleDropoutProbability;
  private TrainingType type = TrainingType.CPU;
  private boolean verbose;
  private ErrorFunction error;
  private boolean stochastic = false;
  private int miniBatchSize;
  private int batchParallelism = Runtime.getRuntime().availableProcessors();

  private MultilayerPerceptron(MultilayerPerceptronBuilder conf) {

    this.layers = conf.layer;
    this.maxIterations = conf.maxIterations;
    this.minimizer = conf.minimizer;
    this.lambda = conf.lambda;
    this.type = conf.type;
    this.hiddenDropoutProbability = conf.hiddenDropoutProbability;
    this.visibleDropoutProbability = conf.visibleDropoutProbability;
    this.verbose = conf.verbose;
    this.error = conf.error;
    this.stochastic = conf.stochastic;
    this.miniBatchSize = conf.miniBatchSize;
    this.batchParallelism = conf.batchParallelism;

    // if the activations are not supplied, we are using standard linear-sigmoid
    // functions
    if (conf.activationFunctions == null) {
      this.activations = new ActivationFunction[layers.length];
      this.activations[0] = new LinearActivationFunction();
      for (int i = 1; i < layers.length; i++) {
        this.activations[i] = new SigmoidActivationFunction();
      }
    } else {
      this.activations = conf.activationFunctions;
    }
    Preconditions.checkArgument(layers.length == activations.length,
        "Size of layers and activations must match!");

    if (conf.weights == null) {
      this.weights = new WeightMatrix[layers.length - 1];
      for (int i = 0; i < weights.length; i++) {
        weights[i] = new WeightMatrix(layers[i], layers[i + 1]);
      }
    } else {
      this.weights = conf.weights;
    }
  }

  /**
   * Custom serialization constructor for already trained networks. Used mainly
   * to query the network after a training session.
   */
  private MultilayerPerceptron(int[] layers, WeightMatrix[] weights,
      ActivationFunction[] activations, ErrorFunction error) {
    this.layers = layers;
    this.weights = weights;
    this.activations = activations;
    this.error = error;
    this.minimizer = null;
    this.maxIterations = -1;
  }

  /**
   * Predicts the outcome of the given input by doing a forward pass.
   */
  @Override
  public DoubleVector predict(DoubleVector xi) {
    DoubleVector activationVector = addBias(xi);
    final int len = layers.length - 1;
    for (int i = 1; i <= len; i++) {
      activationVector = activations[i].apply(weights[i - 1].getWeights()
          .multiplyVectorRow(activationVector));
      // only add the bias if we are not in the last layer
      if (i != len) {
        activationVector = addBias(activationVector);
      }
    }
    return activationVector;
  }

  /**
   * Predicts the outcome of the given input by doing a forward pass. Used for
   * binary classification by a threshold. Everything above threshold will be
   * considered as 1, the other case as 0.
   */
  public DoubleVector predict(DoubleVector xi, double threshold) {
    DoubleVector activations = predict(xi);
    for (int i = 0; i < activations.getLength(); i++) {
      activations.set(i, activations.get(i) > threshold ? 1.0d : 0.0d);
    }

    return activations;
  }

  // could be simplified now due to a "copy" constructor
  private static DoubleVector addBias(DoubleVector activations) {
    return new DenseDoubleVector(1d, activations.toArray());
  }

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    train(features, outcome, minimizer, maxIterations, lambda, verbose);
  }

  /**
   * Full backpropagation training method. It performs weight finding by using a
   * minimizer. Note that it only guarantees to find a global minimum solution
   * in case of linear or convex problems (zero / one hidden layer), of course
   * this is also dependend on the concrete minimizer implementation. If you
   * have more than a single hidden layer, then it will usually trap into a
   * local minimum. It supplies a vector so training can be resumed from a good
   * starting point.
   * 
   * @param features the training examples.
   * @param outcome the outcomes for the training examples.
   * @param minimizer the minimizer to use to train the neural network.
   * @param maxIterations the number of maximum iterations to train.
   * @param lambda the given regularization parameter.
   * @param verbose output to console with the last given errors.
   * @param theta initial spot to start the minimizations.
   * @return the cost of the training.
   */
  public final double train(DoubleVector[] features, DoubleVector[] outcome,
      Minimizer minimizer, int maxIterations, double lambda, boolean verbose) {
    CostFunction costFunction = new MultilayerPerceptronCostFunction(this,
        features, outcome);
    return trainInternal(minimizer, maxIterations, verbose, costFunction,
        getFoldedThetaVector());
  }

  /**
   * Internal training method.
   * 
   * @param minimizer the minimizer to use.
   * @param maxIterations the maximum number of iterations to take.
   * @param verbose output to console with the last given errors.
   * @param costFunction the costfunction to use, normally GPU training uses the
   *          {@link GPUMultilayerPerceptronCostFunction} and the normal
   *          training uses {@link MultilayerPerceptronCostFunction}.
   * @param initialTheta the initial weights to be used (init with initTheta()).
   * @return the cost of the training.
   */
  private double trainInternal(Minimizer minimizer, int maxIterations,
      boolean verbose, CostFunction costFunction, DenseDoubleVector initialTheta) {
    Preconditions.checkNotNull(minimizer, "Minimizer must be supplied!");
    DoubleVector theta = minimizer.minimize(costFunction, initialTheta,
        maxIterations, verbose);
    int[][] unfoldParameters = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(layers);

    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        theta, unfoldParameters);

    for (int i = 0; i < unfoldMatrices.length; i++) {
      getWeights()[i].setWeights(unfoldMatrices[i]);
    }

    return costFunction.evaluateCost(theta).getCost();
  }

  /**
   * @return the folded theta vector, seeded by the current weight matrices.
   */
  public DenseDoubleVector getFoldedThetaVector() {
    // get our randomized weights into a foldable format
    DenseDoubleMatrix[] weightMatrices = new DenseDoubleMatrix[getWeights().length];
    for (int i = 0; i < weightMatrices.length; i++) {
      weightMatrices[i] = getWeights()[i].getWeights();
    }
    return DenseMatrixFolder.foldMatrices(weightMatrices);
  }

  public WeightMatrix[] getWeights() {
    return weights;
  }

  public int[] getLayers() {
    return this.layers;
  }

  public ActivationFunction[] getActivations() {
    return activations;
  }

  double getHiddenDropoutProbability() {
    return this.hiddenDropoutProbability;
  }

  double getVisibleDropoutProbability() {
    return this.visibleDropoutProbability;
  }

  ErrorFunction getErrorFunction() {
    return this.error;
  }

  TrainingType getTrainingType() {
    return this.type;
  }

  double getLambda() {
    return this.lambda;
  }

  int getBatchParallelism() {
    return this.batchParallelism;
  }

  int getMiniBatchSize() {
    return this.miniBatchSize;
  }

  boolean isStochastic() {
    return this.stochastic;
  }

  /**
   * Deserializes a new neural network from the given input stream. Note that
   * "in" will not be closed by this method.
   */
  public static MultilayerPerceptron deserialize(DataInput in)
      throws IOException {
    int numLayers = in.readInt();
    int[] layers = new int[numLayers];
    for (int i = 0; i < numLayers; i++) {
      layers[i] = in.readInt();
    }

    WeightMatrix[] weights = new WeightMatrix[numLayers - 1];
    for (int i = 0; i < weights.length; i++) {
      DenseDoubleMatrix weightMatrix = MatrixWritable.readDenseMatrix(in);
      weights[i] = new WeightMatrix(weightMatrix);
    }

    ActivationFunction[] funcs = new ActivationFunction[numLayers];
    for (int i = 0; i < numLayers; i++) {
      try {
        funcs[i] = (ActivationFunction) Class.forName(in.readUTF())
            .newInstance();
      } catch (InstantiationException | IllegalAccessException
          | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    ErrorFunction error = null;
    try {
      error = (ErrorFunction) Class.forName(in.readUTF()).newInstance();
    } catch (InstantiationException | IllegalAccessException
        | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    return new MultilayerPerceptron(layers, weights, funcs, error);
  }

  /**
   * Serializes this network at its current state to a binary file. Note that
   * "out" will not be closed in this method.
   */
  public static void serialize(MultilayerPerceptron model, DataOutput out)
      throws IOException {
    out.writeInt(model.layers.length);
    // first write all the layers
    for (int l : model.layers) {
      out.writeInt(l);
    }
    // write the weight matrices
    for (WeightMatrix mat : model.weights) {
      DenseDoubleMatrix weights = mat.getWeights();
      MatrixWritable.writeDenseMatrix(weights, out);
    }
    // then write the activation classes
    for (ActivationFunction func : model.activations) {
      out.writeUTF(func.getClass().getName());
    }
    out.writeUTF(model.error.getClass().getName());
  }

}
