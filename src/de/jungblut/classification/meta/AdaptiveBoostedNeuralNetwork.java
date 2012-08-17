package de.jungblut.classification.meta;

import com.google.common.base.Preconditions;

import de.jungblut.classification.nn.MultilayerPerceptron;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Adaptive Boosting neural network learner to improve binary classification
 * accuracy. Currently just for the single hidden layer perceptron and binary
 * classification.
 * 
 * @author thomas.jungblut
 * 
 */
public final class AdaptiveBoostedNeuralNetwork {

  private final boolean singleHiddenLayer;
  private final int[] layers;
  private final int iterations;
  private final MultilayerPerceptron[] classifiers;
  private final DenseDoubleVector beta;

  public AdaptiveBoostedNeuralNetwork(int iterations, int[] layers) {
    this.iterations = iterations;
    this.layers = layers;
    this.singleHiddenLayer = layers.length == 3;
    Preconditions.checkArgument(singleHiddenLayer);
    Preconditions.checkArgument(layers[2] == 1);
    this.classifiers = new MultilayerPerceptron[iterations];
    this.beta = new DenseDoubleVector(iterations);
  }

  /**
   * Trains the meta learner.
   * 
   * @param x training set.
   * @param y prediction set.
   * @param numIterations number of iterations each learner should train.
   * @param lambda the regularization to use.
   * @param predictionThreshold the prediction threshold to distribute the
   *          weights.
   * @param verbose if some debug outputs should be done to the sysout.
   */
  public final void train(DenseDoubleMatrix x, DenseDoubleMatrix y,
      int numIterations, double lambda, double predictionThreshold,
      boolean verbose) {

    DenseDoubleVector weights = new DenseDoubleVector(x.getRowCount(), 1.0d);

    DenseDoubleMatrix trainingSet = x;
    DenseDoubleMatrix trainingSetPrediction = y;

    for (int iteration = 0; iteration < iterations; iteration++) {

      classifiers[iteration] = getNewNetwork();
      double epsilon = classifiers[iteration].trainFmincg(trainingSet,
          trainingSetPrediction, numIterations, lambda, verbose);

      if (iteration > 0 && (epsilon == 0.0d || epsilon > 0.5d)) {
        classifiers[iteration] = null;
        continue;
      }

      double reweight = (1d - epsilon) / epsilon;
      beta.set(iteration, Math.log(reweight));
      reweight(classifiers[iteration], trainingSet, trainingSetPrediction,
          predictionThreshold, weights, reweight);
    }

  }

  /**
   * Reweights the rows of our trainingsets.
   */
  private void reweight(MultilayerPerceptron instance,
      DenseDoubleMatrix trainingSet, DenseDoubleMatrix trainingSetPrediction,
      double threshold, DenseDoubleVector weights, double reweight) {

    double sum = weights.sum();
    for (int row = 0; row < trainingSet.getRowCount(); row++) {
      // TODO weight must be applied to the current prediction..
      DenseDoubleVector prediction = instance.predict(
          trainingSet.getRowVector(row), threshold);
      DoubleVector outcome = trainingSetPrediction.getRowVector(row);
      boolean correct = prediction.subtract(outcome).get(0) == 0.0d ? true
          : false;
      if (!correct) {
        weights.set(row, weights.get(row) * reweight);
      }
    }

    double newSum = weights.sum();
    for (int row = 0; row < trainingSet.getRowCount(); row++) {
      weights.set(row, weights.get(row) * sum / newSum);
    }

  }

  private MultilayerPerceptron getNewNetwork() {
    return new MultilayerPerceptron(layers);
  }

}
