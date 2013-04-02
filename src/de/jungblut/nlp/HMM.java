package de.jungblut.nlp;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.apache.hadoop.io.Writable;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.writable.MatrixWritable;
import de.jungblut.writable.VectorWritable;

/**
 * Hidden Markov Model implementation for multiple observations for all three
 * types of problems HMM aims to solve (Decoding, likelihood estimation,
 * unsupervised/supervised learning).
 * 
 * @author thomas.jungblut
 * 
 */
public final class HMM extends AbstractClassifier implements Writable {

  private int numVisibleStates;
  private int numHiddenStates;

  /**
   * transition matrix of hidden state i (row) to state j column. (A) =
   * numHiddenStates * numHiddenStates.
   */
  private DoubleMatrix transitionProbabilityMatrix;
  /**
   * emission matrix of an probability observation/feature o_t generated from a
   * state i. (in literature called B) = numHiddenStates * numVisibleStates.
   */
  private DoubleMatrix emissionProbabilitiyMatrix;

  /**
   * initial hidden state probabilities (prior how likely a state is happening)
   */
  private DoubleVector hiddenPriorProbability;

  // test seed
  private long seed;

  // deserialization constructor for Writable types
  public HMM() {
    seed = System.currentTimeMillis();
  }

  public HMM(int numVisibleStates, int numHiddenStates) {
    this(numVisibleStates, numHiddenStates, System.currentTimeMillis());
  }

  // test constructor
  HMM(int numVisibleStates, int numHiddenStates, long seed) {
    this.seed = seed;
    this.numVisibleStates = numVisibleStates;
    this.numHiddenStates = numHiddenStates;
    this.transitionProbabilityMatrix = new DenseDoubleMatrix(numHiddenStates,
        numHiddenStates);
    this.emissionProbabilitiyMatrix = new DenseDoubleMatrix(numHiddenStates,
        numVisibleStates);
    this.hiddenPriorProbability = new DenseDoubleVector(numHiddenStates);
  }

  /**
   * Normalizes the values of all three main datastructures of the HMM
   * (transitionProbabilityMatrix, emissionProbabilitiyMatrix and
   * hiddenPriorProbability) by summing the values and dividing each element by
   * the sum. For matrices we are using row sums over the hidden states.
   */
  private void normalizeProbabilities() {
    hiddenPriorProbability = hiddenPriorProbability
        .divide(hiddenPriorProbability.sum());

    for (int row = 0; row < numHiddenStates; row++) {
      // note that we are using row vectors here, because dense matrices give us
      // the underlying array wrapped by the vector object
      DoubleVector rowVector = transitionProbabilityMatrix.getRowVector(row);
      transitionProbabilityMatrix.setRowVector(row,
          rowVector.divide(rowVector.sum()));
      rowVector = emissionProbabilitiyMatrix.getRowVector(row);
      emissionProbabilitiyMatrix.setRowVector(row,
          rowVector.divide(rowVector.sum()));
    }
  }

  /**
   * Likelihood estimation on the current HMM. It estimates the likelihood that
   * the given observation sequence is about to happen. P( O | lambda ) where O
   * is the observation sequence and lambda are the HMM's parameters. This is
   * done by executing the forward algorithm with the given observations clamped
   * to the visible states.
   * 
   * @param observationSequence the given sequence of observations (features).
   * @return the likelihood (not a probability!) that the given sequence is
   *         about to happen.
   */
  public double estimateLikelihood(DoubleVector observationSequence) {
    // TODO
    return 0d;
  }

  /**
   * Decodes the given observation sequence (features) with the current HMM.
   * This discovers the best hidden state sequence Q that is derived by
   * executing the Viterbi algorithm with the given observations and the HMM's
   * parameters lambda.
   * 
   * @param observationSequence the given sequence of features.
   * @return the int array denoting the path through the hidden state sequences
   *         (from 0 to numHiddenStates).
   */
  public int[] decode(DoubleVector observationSequence) {
    // TODO
    return null;
  }

  /**
   * Trains the current models parameters by executing a baum-welch expectation
   * maximization algorithm.
   * 
   * @param features the visible state activations (the vector will be traversed
   *          for non-zero entries, so the value actually doesn't matter).
   * @param outcome the outcome that was assigned to the given features. This
   *          can be in the binary case a single element vector (0d or 1d), or
   *          in the multi-class case a vector which index denotes the class
   *          (from zero to numHiddenStates, activation is again 0d or 1d). Note
   *          that in the multi-class case just a single state can be turned on,
   *          so the classes are mutual exclusive.
   * @param epsilon the absolute difference in the train model to the previous.
   *          If smaller than given value the iterations are stopped and the
   *          training finishes.
   * @param maxIterations if the epsilon threshold is never reached, the maximum
   *          iterations usually applies by stopping computation after given
   *          number of iterations.
   * @param verbose when set to true it will print information about the
   *          expectimax values per iteration.
   */
  public void trainUnsupervised(DoubleVector[] features,
      DenseDoubleVector[] outcome, double epsilon, int maxIterations,
      boolean verbose) {
    // initialize a random starting state
    Random random = new Random(seed);
    transitionProbabilityMatrix = new DenseDoubleMatrix(numHiddenStates,
        numHiddenStates, random);
    emissionProbabilitiyMatrix = new DenseDoubleMatrix(numHiddenStates,
        numVisibleStates, random);
    hiddenPriorProbability = new DenseDoubleVector(numHiddenStates);
    for (int i = 0; i < numHiddenStates; i++) {
      hiddenPriorProbability.set(i, random.nextDouble());
    }
    normalizeProbabilities();

    // TODO train using baum-welch (forward+backward pass, normalize and check
    // for convergence by taking the abs diff of the two probability matrix)
  }

  /**
   * Trains the current models parameters by executing a forwad pass over the
   * given observations (hidden and visible states). Probabilities are +1
   * smoothed while counting in case there would be zero probability somewhere.
   * This method is compatible to the Classifier#train method so this model can
   * be used as a simple classifier.
   * 
   * @param features the visible state activations (the vector will be traversed
   *          for non-zero entries, so the value actually doesn't matter).
   * @param outcome the outcome that was assigned to the given features. This
   *          can be in the binary case a single element vector (0d or 1d), or
   *          in the multi-class case a vector which index denotes the class
   *          (from zero to numHiddenStates, activation is again 0d or 1d). Note
   *          that in the multi-class case just a single state can be turned on,
   *          so the classes are mutual exclusive.
   */
  public void trainSupervised(DoubleVector[] features,
      DenseDoubleVector[] outcome) {
    // first check both have the same length, then sanity check with the
    // parameters
    checkArgument(features.length == outcome.length,
        "Feature array length must match outcome array length: "
            + features.length + " != " + outcome.length);
    // check if we have enough examples (at least 1)
    checkArgument(features.length > 0,
        "Feature array length be at least 1! Given: " + features.length);
    // check if the feature vectors dimension matches the number of visible
    // states
    checkArgument(features[0].getDimension() == numVisibleStates,
        "Feature vector's dimension must match the number of visible states! Given: "
            + features[0].getDimension() + ", but expected " + numVisibleStates);
    // now check if the outcome is sane
    int outcomeDimension = outcome[0].getDimension();
    // this checks whether the outcome dimension is 1, if so we expect binary
    // outcomes, else the number of hidden states
    int expectedDimension = outcomeDimension == 1 ? 2 : numHiddenStates;
    checkArgument(outcomeDimension == expectedDimension,
        "Outcome dimension didn't match the given number of hidden states: "
            + outcomeDimension + " != " + expectedDimension);

    // +1 smooth first
    hiddenPriorProbability = hiddenPriorProbability.add(1d);
    for (int rowIndex = 0; rowIndex < numHiddenStates; rowIndex++) {
      transitionProbabilityMatrix.setRowVector(rowIndex,
          DenseDoubleVector.ones(numHiddenStates));
      emissionProbabilitiyMatrix.setRowVector(rowIndex,
          DenseDoubleVector.ones(numVisibleStates));
    }

    for (int i = 0; i < features.length; i++) {
      DoubleVector feat = features[i];
      DenseDoubleVector out = outcome[i];

      int index = getOutcomeState(out);
      hiddenPriorProbability.set(index, hiddenPriorProbability.get(index) + 1);

      // count the emissions from feature layer to the hidden layer
      Iterator<DoubleVectorElement> iterateNonZero = feat.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        emissionProbabilitiyMatrix.set(next.getIndex(), index,
            emissionProbabilitiyMatrix.get(next.getIndex(), index) + 1);
      }
      // now handle the feature by counting the transitions between the hidden
      // states with the next feature
      if (i + 1 < features.length) {
        DenseDoubleVector nextOut = outcome[i + 1];
        int nextIndex = getOutcomeState(nextOut);
        transitionProbabilityMatrix.set(index, nextIndex,
            transitionProbabilityMatrix.get(index, nextIndex) + 1);
      }
    }

    // now we can divide by the counts and normalize the probabilities so they
    // sum to 1
    normalizeProbabilities();
  }

  @Override
  public void train(DoubleVector[] features, DenseDoubleVector[] outcome) {
    trainSupervised(features, outcome);
  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    // TODO clamp the features to the visible units, make some matrix
    // multiplication and obtain the probability for each hidden state (maybe
    // normalize again)
    return null;
  }

  public DoubleVector predict(DoubleVector features,
      DenseDoubleVector previousOutcome) {
    // TODO clamp the features to the visible units, make some matrix
    // multiplication and obtain the probability for each hidden state (maybe
    // normalize again)
    return null;
  }

  public int getNumHiddenStates() {
    return this.numHiddenStates;
  }

  public int getNumVisibleStates() {
    return this.numVisibleStates;
  }

  public DoubleMatrix getEmissionProbabilitiyMatrix() {
    return this.emissionProbabilitiyMatrix;
  }

  public DoubleVector getHiddenPriorProbability() {
    return this.hiddenPriorProbability;
  }

  public DoubleMatrix getTransitionProbabilityMatrix() {
    return this.transitionProbabilityMatrix;
  }

  /**
   * @return the outcome state as integer that can be treated as index.
   */
  private int getOutcomeState(DenseDoubleVector out) {
    int index;
    if (out.getDimension() == 2) {
      index = (int) out.get(0); // simple cast is enough here
    } else {
      // assume that the max index is correctly set to
      // 1, no other state was ticked on.
      index = out.maxIndex();
    }
    return index;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(numVisibleStates);
    out.writeInt(numHiddenStates);
    VectorWritable.writeVector(hiddenPriorProbability, out);
    MatrixWritable.write(transitionProbabilityMatrix, out);
    MatrixWritable.write(emissionProbabilitiyMatrix, out);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    numVisibleStates = in.readInt();
    numHiddenStates = in.readInt();
    hiddenPriorProbability = VectorWritable.readVector(in);
    transitionProbabilityMatrix = MatrixWritable.read(in);
    emissionProbabilitiyMatrix = MatrixWritable.read(in);
  }

}
