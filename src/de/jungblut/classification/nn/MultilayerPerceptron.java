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
import de.jungblut.math.activation.SoftMaxActivationFunction;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.writable.MatrixWritable;

/**
 * Multilayer perceptron implementation that works on GPU via JCuda and CPU. It
 * features l1 regularization and dropout as well as a variety of activation
 * functions and error functions that can be configured.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MultilayerPerceptron extends AbstractClassifier {

  /**
   * Train normally on the CPU or on the GPU via CUDA?
   */
  public static enum TrainingType {
    CPU, GPU
  }

  public static long SEED = System.currentTimeMillis();

  /**
   * Configuration for training a neural net through the {@link Classifier}
   * interfaces.
   * 
   */
  public static final class TrainingConfiguration {
    TrainingType type;
    Minimizer minimizer;
    int maxIterations;
    double lambda;
    boolean verbose;
    ActivationFunction[] activations;
    double hiddenDropoutProbability;
    double visibleDropoutProbability;

    public TrainingConfiguration(Minimizer minimizer,
        ActivationFunction[] activations, int maxIterations, double lambda,
        boolean verbose) {
      this(TrainingType.CPU, minimizer, activations, maxIterations, lambda,
          verbose, 0d, 0d);
    }

    // TODO replace this with a builder pattern
    public TrainingConfiguration(TrainingType type, Minimizer minimizer,
        ActivationFunction[] activations, int maxIterations, double lambda,
        boolean verbose, double hiddenDropoutProbability,
        double visibleDropoutProbability) {
      this.visibleDropoutProbability = visibleDropoutProbability;
      this.hiddenDropoutProbability = hiddenDropoutProbability;
      this.type = type;
      this.activations = activations;
      this.minimizer = minimizer;
      this.maxIterations = maxIterations;
      this.lambda = lambda;
      this.verbose = verbose;
    }

  }

  private final WeightMatrix[] weights;
  private final int[] layers;
  private final ActivationFunction[] activations;

  private ErrorFunction error = ErrorFunction.SIGMOID_ERROR;
  private TrainingConfiguration conf;
  double hiddenDropoutProbability;
  double visibleDropoutProbability;

  /**
   * Multilayer perceptron initializer by using an int[] to describe the number
   * of units per layer.<br/>
   * For example if you want to solve the XOR problem by 2 input neurons, a
   * hidden layer with 3 neurons and an output layer with one neuron, you can
   * feed this contructor with int[]{2,3,1}.
   */
  public MultilayerPerceptron(int[] layer, ActivationFunction[] activations) {
    // if the activations are not supplied, we are using standard linear-sigmoid
    // functions
    if (activations == null) {
      this.activations = new ActivationFunction[layer.length];
      this.activations[0] = new LinearActivationFunction();
      for (int i = 1; i < layer.length; i++) {
        this.activations[i] = new SigmoidActivationFunction();
      }
    } else {
      this.activations = activations;
    }
    Preconditions.checkArgument(layer.length == activations.length,
        "Size of layers and activations must match!");
    this.layers = layer;

    weights = new WeightMatrix[layers.length - 1];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = new WeightMatrix(layers[i], layers[i + 1]);
    }

    // choose the error function based on the output layer

    ActivationFunction function = activations[layers.length - 1];
    if (function instanceof SigmoidActivationFunction) {
      error = ErrorFunction.SIGMOID_ERROR;
    } else if (function instanceof LinearActivationFunction) {
      error = ErrorFunction.SQUARED_MEAN_ERROR;
    } else if (function instanceof SoftMaxActivationFunction) {
      error = ErrorFunction.SOFTMAX_ERROR;
    } else {
      error = ErrorFunction.SIGMOID_ERROR;
    }
  }

  /**
   * Multilayer perceptron initializer by using an int[] to describe the number
   * of units per layer.<br/>
   * For example if you want to solve the XOR problem by 2 input neurons, a
   * hidden layer with 3 neurons and an output layer with one neuron, you can
   * feed this contructor with int[]{2,3,1}.
   * 
   * @param conf the configuration to train with through the {@link Classifier}
   *          interface.
   */
  public MultilayerPerceptron(int[] layer, TrainingConfiguration conf) {
    this(layer, conf.activations);
    this.conf = conf;
    this.hiddenDropoutProbability = this.conf.hiddenDropoutProbability;
    this.visibleDropoutProbability = this.conf.visibleDropoutProbability;
  }

  /**
   * Custom serialization constructor for already trained networks.
   */
  public MultilayerPerceptron(int[] layers, WeightMatrix[] weights,
      ActivationFunction[] activations) {
    this.layers = layers;
    this.weights = weights;
    this.activations = activations;
  }

  /**
   * Predicts the outcome of the given input by doing a forward pass.
   */
  @Override
  public DenseDoubleVector predict(DoubleVector xi) {
    DoubleVector activationVector = addBias(xi);
    final int len = layers.length - 1;
    for (int i = 1; i <= len; i++) {
      activationVector = activations[i].apply(weights[i - 1].getWeights()
          .multiplyVector(activationVector));
      // only add the bias if we are not in the last layer
      if (i != len) {
        activationVector = addBias(activationVector);
      }
    }
    return (DenseDoubleVector) activationVector;
  }

  /**
   * Predicts the outcome of the given input by doing a forward pass. Used for
   * binary classification by a threshold. Everything above threshold will be
   * considered as 1, the other case as 0.
   */
  public DenseDoubleVector predict(DoubleVector xi, double threshold) {
    DenseDoubleVector activations = predict(xi);
    for (int i = 0; i < activations.getLength(); i++) {
      activations.set(i, activations.get(i) > threshold ? 1.0d : 0.0d);
    }

    return activations;
  }

  private static DoubleVector addBias(DoubleVector activations) {
    DenseDoubleVector v = new DenseDoubleVector(activations.getLength() + 1);
    v.set(0, 1.0d); // bias unit is always at index zero
    for (int i = 0; i < activations.getLength(); i++) {
      v.set(i + 1, activations.get(i));
    }
    return v;
  }

  @Override
  public void train(DoubleVector[] features, DenseDoubleVector[] outcome) {
    Preconditions
        .checkNotNull(
            conf,
            "Configuration shouldn't be null here, have you used the correct constructor to provide this configuration?");

    if (conf.type == TrainingType.CPU) {
      train(new DenseDoubleMatrix(features), new DenseDoubleMatrix(outcome),
          conf.minimizer, conf.maxIterations, conf.lambda, conf.verbose);
    } else {
      trainGPU(new DenseDoubleMatrix(features), new DenseDoubleMatrix(outcome),
          conf.minimizer, conf.maxIterations, conf.lambda, conf.verbose);
    }
  }

  /**
   * Full backpropagation training method. It performs weight finding by using a
   * minimizer. Note that it only guarantees to find a global minimum solution
   * in case of linear or convex problems (zero / one hidden layer), of course
   * this is also dependend on the concrete minimizer implementation. If you
   * have more than a single hidden layer, then it will usually trap into a
   * local minimum.
   * 
   * @param x the training examples.
   * @param y the outcomes for the training examples.
   * @param minimizer the minimizer to use to train the neural network.
   * @param maxIterations the number of maximum iterations to train.
   * @param lambda the given regularization parameter.
   * @param verbose output to console with the last given errors.
   * @return the cost of the training.
   */
  public final double train(DenseDoubleMatrix x, DenseDoubleMatrix y,
      Minimizer minimizer, int maxIterations, double lambda, boolean verbose) {
    CostFunction costFunction = new MultilayerPerceptronCostFunction(this, x,
        y, lambda);
    return trainInternal(minimizer, maxIterations, verbose, costFunction,
        getFoldedThetaVector());
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
   * @param x the training examples.
   * @param y the outcomes for the training examples.
   * @param minimizer the minimizer to use to train the neural network.
   * @param maxIterations the number of maximum iterations to train.
   * @param lambda the given regularization parameter.
   * @param verbose output to console with the last given errors.
   * @param theta initial spot to start the minimizations.
   * @return the cost of the training.
   */
  public final double train(DenseDoubleMatrix x, DenseDoubleMatrix y,
      Minimizer minimizer, int maxIterations, double lambda, boolean verbose,
      DenseDoubleVector theta) {
    CostFunction costFunction = new MultilayerPerceptronCostFunction(this, x,
        y, lambda);
    return trainInternal(minimizer, maxIterations, verbose, costFunction, theta);
  }

  /**
   * Full backpropagation training method on the GPU. It performs weight finding
   * by using a minimizer. Note that it only guarantees to find a global minimum
   * solution in case of linear or convex problems (zero / one hidden layer), of
   * course this is also dependend on the concrete minimizer implementation. If
   * you have more than a single hidden layer, then it will usually trap into a
   * local minimum. It supplies a vector so training can be resumed from a good
   * starting point.
   * 
   * @param x the training examples.
   * @param y the outcomes for the training examples.
   * @param minimizer the minimizer to use to train the neural network.
   * @param maxIterations the number of maximum iterations to train.
   * @param lambda the given regularization parameter.
   * @param verbose output to console with the last given errors.
   * @param theta initial spot to start the minimizations.
   * @return the cost of the training.
   */
  public final double trainGPU(DenseDoubleMatrix x, DenseDoubleMatrix y,
      Minimizer minimizer, int maxIterations, double lambda, boolean verbose,
      DenseDoubleVector theta) {
    CostFunction costFunction = new GPUMultilayerPerceptronCostFunction(this,
        x, y, lambda);
    return trainInternal(minimizer, maxIterations, verbose, costFunction, theta);
  }

  /**
   * Full backpropagation training method on the GPU. It performs weight finding
   * by using a minimizer. Note that it only guarantees to find a global minimum
   * solution in case of linear or convex problems (zero / one hidden layer), of
   * course this is also dependend on the concrete minimizer implementation. If
   * you have more than a single hidden layer, then it will usually trap into a
   * local minimum.
   * 
   * @param x the training examples.
   * @param y the outcomes for the training examples.
   * @param minimizer the minimizer to use to train the neural network.
   * @param maxIterations the number of maximum iterations to train.
   * @param lambda the given regularization parameter.
   * @param verbose output to console with the last given errors.
   * @return the cost of the training.
   */
  public final double trainGPU(DenseDoubleMatrix x, DenseDoubleMatrix y,
      Minimizer minimizer, int maxIterations, double lambda, boolean verbose) {
    CostFunction costFunction = new GPUMultilayerPerceptronCostFunction(this,
        x, y, lambda);
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
    DoubleVector theta = minimizer.minimize(costFunction, initialTheta,
        maxIterations, verbose);
    int[][] unfoldParameters = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(layers);

    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        theta, unfoldParameters);

    for (int i = 0; i < unfoldMatrices.length; i++) {
      getWeights()[i].setWeights(unfoldMatrices[i]);
    }

    return costFunction.evaluateCost(theta).getFirst();
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

  public ErrorFunction getError() {
    return this.error;
  }

  public void setConfiguration(TrainingConfiguration conf) {
    this.conf = conf;
  }

  public double getHiddenDropoutProbability() {
    return this.hiddenDropoutProbability;
  }

  public double getVisibleDropoutProbability() {
    return this.visibleDropoutProbability;
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
      DenseDoubleMatrix weightMatrix = (DenseDoubleMatrix) MatrixWritable
          .read(in);
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

    return new MultilayerPerceptron(layers, weights, funcs);
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
      MatrixWritable.write(weights, out);
    }
    // then write the activation classes
    for (ActivationFunction func : model.activations) {
      out.writeUTF(func.getClass().getName());
    }
  }

}
