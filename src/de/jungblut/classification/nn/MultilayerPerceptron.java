package de.jungblut.classification.nn;

import gnu.trove.list.array.TDoubleArrayList;

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
import de.jungblut.writable.VectorWritable;

/**
 * Multilayer perceptron by my collegue Marvin Ritter using my math library. <br/>
 * I have changed it to a format that can be used for batch training for example
 * in a clustered environment.<br/>
 * Also I have added several minimizers that will find minimas.
 * 
 * @author marvin.ritter
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

    public TrainingConfiguration(Minimizer minimizer,
        ActivationFunction[] activations, int maxIterations, double lambda,
        boolean verbose) {
      this.activations = activations;
      this.type = TrainingType.CPU;
      this.minimizer = minimizer;
      this.maxIterations = maxIterations;
      this.lambda = lambda;
      this.verbose = verbose;
    }

    public TrainingConfiguration(TrainingType type,
        ActivationFunction[] activations, Minimizer minimizer,
        int maxIterations, double lambda, boolean verbose) {
      this.type = type;
      this.activations = activations;
      this.minimizer = minimizer;
      this.maxIterations = maxIterations;
      this.lambda = lambda;
      this.verbose = verbose;
    }

  }

  private final WeightMatrix[] weights;
  private final Layer[] layers;
  private final ActivationFunction[] activations;
  private ErrorFunction error = ErrorFunction.SIGMOID_ERROR;
  private TrainingConfiguration conf;

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
    this.layers = new Layer[layer.length];
    for (int i = 0; i < layer.length; i++) {
      layers[i] = new Layer(layer[i]);
    }

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
  }

  /**
   * Custom serialization constructor for already trained networks.
   */
  public MultilayerPerceptron(Layer[] layers, WeightMatrix[] weights,
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

  private DoubleVector addBias(DoubleVector activations) {
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
   * Full backpropagation training method. It checks whether the maximum
   * iterations have been exceeded or a given error has been archived.
   * 
   * @deprecated this is deprecated, because this is a bit buggy. Please use the
   *             other methods that use a optimizer.
   * @param x the training examples.
   * @param y the outcomes for the training examples.
   * @param maxIterations the number of maximum iterations to train.
   * @param maximumError the maximum error when training can be stopped.
   * @param learningRate the given learning rate.
   * @param lambda the given regularization parameter.
   * @param verbose output to console with the last given errors.
   * @return the last squared mean error.
   */
  @Deprecated
  public double train(DenseDoubleMatrix x, DenseDoubleMatrix y,
      int maxIterations, double maximumError, double learningRate,
      double lambda, boolean verbose) {
    TDoubleArrayList errorList = new TDoubleArrayList();
    int iteration = 0;
    while (iteration < maxIterations) {
      double mse = 0.0d;
      resetGradients();
      for (int i = 0; i < x.getRowCount(); i++) {
        DoubleVector difference = forwardStep(x.getRowVector(i),
            y.getRowVector(i));
        mse += difference.pow(2).sum();
        backwardStep(difference);
      }
      adjustWeights(x.getRowCount(), learningRate, lambda);
      if (verbose) {
        System.out.println(iteration + ": " + mse);
      }
      errorList.add(mse);
      iteration++;
      // stop learning if we have reached our maximum error.
      if (mse < maximumError)
        break;
    }
    return errorList.getQuick(errorList.size() - 1);
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
    return trainInternal(minimizer, maxIterations, verbose, costFunction);
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
    return trainInternal(minimizer, maxIterations, verbose, costFunction);
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
   * @return the cost of the training.
   */
  private double trainInternal(Minimizer minimizer, int maxIterations,
      boolean verbose, CostFunction costFunction) {
    // get our randomized weights into a foldable format
    DenseDoubleMatrix[] weightMatrices = new DenseDoubleMatrix[getWeights().length];
    for (int i = 0; i < weightMatrices.length; i++)
      weightMatrices[i] = getWeights()[i].getWeights();

    DenseDoubleVector pInput = DenseMatrixFolder.foldMatrices(weightMatrices);
    DoubleVector theta = minimizer.minimize(costFunction, pInput,
        maxIterations, verbose);
    // compute the layer sizes to unfold the matrices correctly
    int[] layerSizes = new int[layers.length];
    for (int i = 0; i < layerSizes.length; i++) {
      layerSizes[i] = layers[i].getLength();
    }
    int[][] unfoldParameters = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(layerSizes);

    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        theta, unfoldParameters);

    for (int i = 0; i < unfoldMatrices.length; i++) {
      getWeights()[i].setWeights(unfoldMatrices[i]);
    }

    return costFunction.evaluateCost(theta).getFirst();
  }

  public WeightMatrix[] getWeights() {
    return weights;
  }

  public Layer[] getLayers() {
    return layers;
  }

  public ActivationFunction[] getActivations() {
    return activations;
  }

  public ErrorFunction getError() {
    return this.error;
  }

  /**
   * At the beginning of each batch forward propagation we should reset the
   * gradients.
   */
  private void resetGradients() {
    // reset all gradients / derivatives
    for (WeightMatrix weight : weights) {
      weight.resetDerivatives();
    }
  }

  /**
   * Do a forward step and calculate the difference between the outcome and the
   * prediction.
   */
  private DoubleVector forwardStep(DoubleVector x, DoubleVector outcome) {
    DenseDoubleVector prediction = predict(x);
    return prediction.subtract(outcome);
  }

  /**
   * Do a backward step by the given error in the last layer.
   */
  private void backwardStep(DoubleVector errorLastLayer) {
    // set error of last layer and then use backward propagation the calculate
    // the errors of the other layers
    // the first layer can be left out, cause the input error is not wrong ;)
    layers[layers.length - 1].setErrors(errorLastLayer);
    for (int k = weights.length - 1; k > 0; k--) {
      weights[k].backwardError();
    }

    // based on the errors we can now calculate the partial derivatives for all
    // layers and add them to the
    // internal matrix
    for (WeightMatrix weight : weights) {
      weight.addDerivativesFromError();
    }
  }

  /**
   * After a full forward and backward step we can adjust the weights by
   * normalizing the via the learningrate and lambda. Here we also need the
   * number of training examples seen.<br/>
   */
  private void adjustWeights(int numTrainingExamples, double learningRate,
      double lambda) {
    // adjust weights using the given learning rate
    for (WeightMatrix weight : weights) {
      weight.updateWeights(numTrainingExamples, learningRate, lambda);
    }
  }

  /**
   * Deserializes a new neural network from the given input stream. Note that in
   * will not be closed by this method.
   */
  public static MultilayerPerceptron deserialize(DataInput in)
      throws IOException {
    int numLayers = in.readInt();
    Layer[] layers = new Layer[numLayers];
    for (int i = 0; i < numLayers; i++) {
      int layerLength = in.readInt();
      DoubleVector activations = VectorWritable.readVector(in);
      DoubleVector errors = VectorWritable.readVector(in);
      layers[i] = new Layer(layerLength, (DenseDoubleVector) activations,
          (DenseDoubleVector) errors);
    }

    WeightMatrix[] weights = new WeightMatrix[numLayers - 1];
    for (int i = 0; i < weights.length; i++) {
      DenseDoubleMatrix derivatives = (DenseDoubleMatrix) MatrixWritable
          .read(in);
      DenseDoubleMatrix weightMatrix = (DenseDoubleMatrix) MatrixWritable
          .read(in);
      weights[i] = new WeightMatrix(layers[i], layers[i + 1], weightMatrix,
          derivatives);
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
   * out will not be closed in this method.
   */
  public static void serialize(MultilayerPerceptron model, DataOutput out)
      throws IOException {
    out.writeInt(model.layers.length);
    // first write all the layers
    for (Layer l : model.layers) {
      out.writeInt(l.getLength());
      VectorWritable.writeVector(l.getActivations(), out);
      VectorWritable.writeVector(l.getErrors(), out);
    }
    // write the weight matrices
    for (WeightMatrix mat : model.weights) {
      DenseDoubleMatrix derivatives = mat.getDerivatives();
      MatrixWritable.write(derivatives, out);
      DenseDoubleMatrix weights = mat.getWeights();
      MatrixWritable.write(weights, out);
    }
    // then write the activation classes
    for (ActivationFunction func : model.activations) {
      out.writeUTF(func.getClass().getName());
    }
  }

}
