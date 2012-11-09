package de.jungblut.nlp;

import java.util.List;

import de.jungblut.math.sparse.SparseDoubleColumnMatrix;

/**
 * Markov chain, that can "learn" the state transition probabilities by a given
 * input and returns the probability for a given sequence of states.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MarkovChain {

  // log normalized, so we can sum the probabilities instead of multiplying and
  // running into numerical problems.
  private final SparseDoubleColumnMatrix transitionProbabilities;
  private final int numStates;

  private MarkovChain(int numStates) {
    this(numStates, new SparseDoubleColumnMatrix(numStates, numStates));
  }

  private MarkovChain(int numStates, SparseDoubleColumnMatrix mat) {
    this.numStates = numStates;
    this.transitionProbabilities = mat;
  }

  /**
   * Trains the transition probabilities of the markov chain. <br/>
   * Each list element contains a set of states. The values of the
   * element-states are nominal and should be lower than the number of provided
   * states.
   */
  public void train(List<int[]> states) {
    // TODO

  }

  /**
   * Calculates the probability that the given sequence occurs.
   * 
   * @return value between 0d and 1d, where 1d is very likely that the sequence
   *         is happening.
   */
  public double getProbabilityForSequence(int[] stateSequence) {
    // TODO
    return 0d;
  }

  /**
   * Completes the given state sequence by picking the highest transition
   * probability between the states of the incomplete states.
   */
  public int[] completeStateSequence(int[] stateSequence,
      int... unsuppliedStateIndices) {
    // TODO
    return stateSequence;
  }

  /**
   * @return the state transition probability matrix to export/serialize.
   */
  public SparseDoubleColumnMatrix getTransitionProbabilities() {
    return transitionProbabilities;
  }

  /**
   * @return how many states were defined?
   */
  public int getNumStates() {
    return numStates;
  }

  /**
   * Creates a new markov chain with the supplied number of states.
   */
  public static MarkovChain create(int numStates) {
    return new MarkovChain(numStates);
  }

  /**
   * Creates a new markov chain with the supplied number of states and its
   * predefined transition matrix.
   */
  public static MarkovChain create(int numStates, SparseDoubleColumnMatrix mat) {
    return new MarkovChain(numStates, mat);
  }

}
