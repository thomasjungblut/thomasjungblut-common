package de.jungblut.nlp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.base.Optional;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;

/**
 * Markov chain, that can "learn" the state transition probabilities by a given
 * input and returns the probability for a given sequence of states.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MarkovChain {

  /*
   * log normalized, so we can sum the probabilities instead of multiplying and
   * running into numerical problems. the probability from state i to j can be
   * retrieved by matrix.get(j,i) because it is transposed.
   */
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
   * states (each nominal will be a index, so it's from 0 to numStates-1). So
   * each element can be arbitrary sized, because in markov chains we are
   * considering the transition between two states, thus it will measure the
   * occurrence of each following two state pairs. e.G. [ 1, 2, 3, 4 ] will
   * measure the probabilities of [1,2],[2,3],[3,4].
   */
  public void train(List<int[]> states) {
    // loop over all state sets and set the count of the co-occurrence in the
    // transition probability matrix
    for (int[] array : states) {
      for (int i = 0; i < array.length - 1; i++) {
        // the matrix is transposed, so state n will be in the column, the
        // transition will be in the n+1th row of state n.
        // that's majorly because the sum over columns is much faster than rows
        int count = (int) transitionProbabilities.get(array[i + 1], array[i]);
        transitionProbabilities.set(array[i + 1], array[i], ++count);
      }
    }

    final int[] columnIndices = transitionProbabilities.columnIndices();

    for (int columnIndex : columnIndices) {
      DoubleVector columnVector = transitionProbabilities
          .getColumnVector(columnIndex);
      double sum = columnVector.sum();
      Iterator<DoubleVectorElement> iterateNonZero = columnVector
          .iterateNonZero();
      // loop over all counts and take the log of the probability
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        int row = next.getIndex();
        double probability = Math.log(next.getValue() / sum);
        transitionProbabilities.set(row, columnIndex, probability);
      }
    }

  }

  /**
   * Calculates the probability that the given sequence occurs.
   * 
   * @return value between 0d and 1d, where 1d is very likely that the sequence
   *         is happening.
   */
  public double getProbabilityForSequence(int[] stateSequence) {
    DenseDoubleVector distribution = new DenseDoubleVector(
        stateSequence.length - 1);
    for (int i = 0; i < distribution.getDimension(); i++) {
      distribution.set(i,
          transitionProbabilities.get(stateSequence[i + 1], stateSequence[i]));
    }

    // normalize it by the maximum of the log probabilities
    double max = distribution.max();
    double probabilitySum = 0.0d;
    for (int i = 0; i < distribution.getDimension(); i++) {
      double probability = distribution.get(i);
      double normalizedProbability = probability - max;
      // add up the log probabilities
      probabilitySum += normalizedProbability;
    }
    // no we can exp them to get the real probability
    return Math.exp(probabilitySum);
  }

  /**
   * Completes the given state sequence by picking the best next state on the
   * transition probabilities (so a transition with a high probability is picked
   * more often). If the optional random is not provided, it picks the next
   * state by the highest transition probability between the states (it predicts
   * the next state based on the previous).
   */
  public int[] completeStateSequence(Optional<Random> optionalRandom,
      int[] stateSequence, int... unsuppliedStateIndices) {
    // sort them first, then work through the array
    Arrays.sort(unsuppliedStateIndices);
    for (int index : unsuppliedStateIndices) {
      if (index == 0) {
        // special case because there is no previous state, pick the next
        if (index + 1 < stateSequence.length) {
          if (optionalRandom.isPresent()) {
            stateSequence[index] = chooseState(optionalRandom.get(),
                transitionProbabilities.getRowVector(stateSequence[index + 1]));
          } else {
            stateSequence[index] = transitionProbabilities.getRowVector(
                stateSequence[index + 1]).maxIndex();
          }
        } else {
          throw new IllegalArgumentException("Can't guess state " + index
              + " in " + Arrays.toString(stateSequence));
        }
      } else {
        if (optionalRandom.isPresent()) {
          stateSequence[index] = chooseState(optionalRandom.get(),
              transitionProbabilities.getColumnVector(stateSequence[index - 1]));
        } else {
          stateSequence[index] = transitionProbabilities.getColumnVector(
              stateSequence[index - 1]).maxIndex();
        }
      }
    }
    return stateSequence;
  }

  /**
   * Chooses the state by a uniformly distributed random number, so higher
   * probable states are more likely to happen.
   * 
   * @return the index of the next state.
   */
  private static int chooseState(Random random, DoubleVector probabilities) {
    final double r = random.nextDouble();
    Iterator<DoubleVectorElement> iterateNonZero = probabilities
        .iterateNonZero();
    DoubleVectorElement next = null;
    while (iterateNonZero.hasNext()) {
      next = iterateNonZero.next();
      if (r <= Math.exp(next.getValue())) {
        return next.getIndex();
      }
    }
    // return the last if we haven't escaped yet
    return next.getIndex();
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
