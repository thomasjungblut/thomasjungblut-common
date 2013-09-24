package de.jungblut.classification.sparse;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.activation.SigmoidActivationFunction;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
import de.jungblut.math.squashing.HammingLossFunction;
import de.jungblut.math.tuple.Tuple;

/**
 * Online regression for multi label prediction. It uses stochastic gradient
 * descent to optimize the hamming loss. If a weight value drops below 1e-6, it
 * will consider the weight as zero- thus sparsifiying the matrix on the fly.
 * 
 * @author thomas.jungblut
 * 
 */
public final class SparseMultiLabelRegression {

  private static final SigmoidActivationFunction SIGMOID = new SigmoidActivationFunction();
  private static final HammingLossFunction LOSS = new HammingLossFunction(0.5);
  private static final Random RANDOM = new Random();

  private final double alpha;
  private final int epochs;
  private DoubleMatrix weights;

  private int reportInterval = 500;
  private double nearZeroLimit = 1e-6;
  private boolean verbose = false;

  /**
   * Creates a new multilabel regression.
   * 
   * @param epochs the number of epochs (full passes over the training data).
   * @param alpha the learning rate.
   * @param numFeatures the number of features to expect.
   * @param numOutcomes the number of labels to expect.
   */
  public SparseMultiLabelRegression(int epochs, double alpha, int numFeatures,
      int numOutcomes) {
    this.epochs = epochs;
    this.alpha = alpha;
    this.weights = new SparseDoubleRowMatrix(numFeatures, numOutcomes);
  }

  public void train(Iterable<Tuple<DoubleVector, DoubleVector>> dataStream) {
    DoubleMatrix theta = this.weights;
    initWeights(dataStream, theta);
    for (int epoch = 0; epoch < epochs; epoch++) {
      double avgLoss = 0d;
      int localItems = 0;
      for (Tuple<DoubleVector, DoubleVector> tuple : dataStream) {
        localItems++;
        DoubleVector feature = tuple.getFirst();
        DoubleVector outcome = tuple.getSecond();
        DoubleVector z1 = theta.multiplyVectorColumn(feature);
        DoubleVector activations = SIGMOID.apply(z1);
        double loss = LOSS.calculateError(
            new SparseDoubleRowMatrix(Arrays.asList(outcome)),
            new SparseDoubleRowMatrix(Arrays.asList(activations)));
        avgLoss += loss;
        DoubleVector activationDifference = activations.subtract(outcome);
        // update theta by a smarter sparsity algorithm
        Iterator<DoubleVectorElement> featureIterator = feature
            .iterateNonZero();
        while (featureIterator.hasNext()) {
          DoubleVectorElement next = featureIterator.next();
          DoubleVector rowVector = theta.getRowVector(next.getIndex());
          Iterator<DoubleVectorElement> diffIterator = activationDifference
              .iterateNonZero();
          while (diffIterator.hasNext()) {
            DoubleVectorElement diffElement = diffIterator.next();
            double val = rowVector.get(diffElement.getIndex());
            val = val - diffElement.getValue() * alpha;
            if (Math.abs(val) < nearZeroLimit) {
              val = 0;
            }
            rowVector.set(diffElement.getIndex(), val);
          }
        }
        if (verbose && localItems % reportInterval == 0) {
          System.out.format(" Item %d | AVG Loss: %f\r", localItems,
              (avgLoss / localItems));
        }
      }
      avgLoss /= localItems;
      if (verbose) {
        System.out.format("\nEpoch %d | AVG Loss: %f\n", epoch, avgLoss);
      }
    }

    this.weights = theta;

  }

  /**
   * @param nearZeroLimit sets the limit when to consider a weight to be really
   *          zero.
   */
  public SparseMultiLabelRegression setNearZeroLimit(double nearZeroLimit) {
    this.nearZeroLimit = nearZeroLimit;
    return this;
  }

  /**
   * @param reportInterval the report interval (after how many items seen) in
   *          each epoch.
   */
  public SparseMultiLabelRegression setReportInterval(int reportInterval) {
    this.reportInterval = reportInterval;
    return this;
  }

  /**
   * @return sets this regression into verbose mode.
   */
  public SparseMultiLabelRegression verbose() {
    this.verbose = true;
    return this;
  }

  /**
   * @return the learned weights as a sparse matrix.
   */
  public DoubleMatrix getWeights() {
    return this.weights;
  }

  /**
   * @return a prediction based on the weights and the given input vector.
   */
  public DoubleVector predict(DoubleVector vec) {
    return SIGMOID.apply(weights.multiplyVectorColumn(vec));
  }

  private void initWeights(
      Iterable<Tuple<DoubleVector, DoubleVector>> dataStream, DoubleMatrix theta) {
    for (Tuple<DoubleVector, DoubleVector> tuple : dataStream) {
      // randomly initialize our weight matrix by the data we have seen
      DoubleVector feature = tuple.getFirst();
      DoubleVector outcome = tuple.getSecond();
      Iterator<DoubleVectorElement> featureIterator = feature.iterateNonZero();
      while (featureIterator.hasNext()) {
        DoubleVectorElement feat = featureIterator.next();
        Iterator<DoubleVectorElement> outcomeIterator = outcome
            .iterateNonZero();
        while (outcomeIterator.hasNext()) {
          DoubleVectorElement out = outcomeIterator.next();
          theta.set(feat.getIndex(), out.getIndex(), RANDOM.nextDouble());
        }
      }
    }
  }

}
