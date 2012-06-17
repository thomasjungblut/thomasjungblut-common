package de.jungblut.classification.nn;

import gnu.trove.list.array.TDoubleArrayList;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Multilayer perceptron by my collegue Marvin Ritter using my math library. <br/>
 * I have changed it to a format that can be used for batch training for example
 * in a clustered environment.<br/>
 * Error function is the mean squared error and activation function in each
 * neuron is the sigmoid function.
 * 
 * @author marvin.ritter
 * @author thomas.jungblut
 * 
 */
public final class MultilayerPerceptron {

  private final WeightMatrix[] weights;
  private final Layer[] layers;

  /**
   * Multilayer perceptron initializer by using an int[] to describe the number
   * of units per layer.<br/>
   * For example if you want to solve the XOR problem by 2 input neurons, a
   * hidden layer with 3 neurons and an output layer with one neuron, you can
   * feed this contructor with int[]{2,3,1}.
   */
  public MultilayerPerceptron(int[] layer) {
    this.layers = new Layer[layer.length];
    for (int i = 0; i < layer.length; i++) {
      layers[i] = new Layer(layer[i]);
    }

    weights = new WeightMatrix[layers.length - 1];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = new WeightMatrix(layers[i], layers[i + 1]);
    }
  }

  /**
   * Predicts the outcome of the given input by doing a forward pass.
   */
  public DenseDoubleVector predict(DoubleVector xi) {
    layers[0].setActivations(xi);

    for (int i = 0; i < weights.length; i++) {
      weights[i].forward();
    }

    return layers[layers.length - 1].getActivations();
  }

  /**
   * At the beginning of each batch forward propagation we should reset the
   * gradients.
   */
  public void resetGradients() {
    // reset all gradients / derivatives
    for (int k = 0; k < weights.length; k++) {
      weights[k].resetDerivatives();
    }
  }

  /**
   * Do a forward step and calculate the difference between the outcome and the
   * prediction.
   */
  public DoubleVector forwardStep(DoubleVector x, DoubleVector outcome) {
    DenseDoubleVector prediction = predict(x);
    return prediction.subtract(outcome);
  }

  /**
   * Do a backward step by the given error in the last layer.
   */
  public void backwardStep(DoubleVector errorLastLayer) {
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
    for (int k = 0; k < weights.length; k++) {
      weights[k].addDerivativesFromError();
    }
  }

  /**
   * After a full forward and backward step we can adjust the weights by
   * normalizing the via the learningrate and lambda. Here we also need the
   * number of training examples seen.
   */
  public void adjustWeights(int numTrainingExamples, double learningRate,
      double lambda) {
    // adjust weights using the given learning rate
    for (int k = 0; k < weights.length; k++) {
      weights[k].updateWeights(numTrainingExamples, learningRate, lambda);
    }
  }

  /**
   * Full backpropagation training method. It checks whether the maximum
   * iterations have been exceeded or a given error has been archived.
   * 
   * @param x the training examples
   * @param y the outcomes for the training examples
   * @param maxIterations the number of maximum iterations to train
   * @param maximumError the maximum error when training can be stopped
   * @param learningRate the given learning rate
   * @param lambda the given regularization parameter
   * @param verbose output to console with the last given errors
   * @return the squared mean errors per iteration, can be used to plot learning
   *         curves
   */
  public DoubleVector train(DoubleVector[] x, DoubleVector[] y,
      int maxIterations, double maximumError, double learningRate,
      double lambda, boolean verbose) {
    TDoubleArrayList errorList = new TDoubleArrayList();
    int iteration = 0;
    while (iteration < maxIterations) {
      double mse = 0.0d;
      resetGradients();
      for (int i = 0; i < x.length; i++) {
        DoubleVector difference = forwardStep(x[i], y[i]);
        mse += difference.pow(2).sum();
        backwardStep(difference);
      }
      adjustWeights(x.length, 1.0, 0.0d);
      if (verbose) {
        System.out.println(iteration + ": " + mse);
      }
      errorList.add(mse);
      iteration++;
      // stop learning if we have reached our maximum error.
      if (mse < maximumError)
        break;
    }
    return new DenseDoubleVector(errorList.toArray());
  }
}
