package de.jungblut.pgm;

import java.util.List;

import com.google.common.base.Preconditions;

import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * A hidden markov model trained by a Baum Welch algorithm.
 */
public final class HMM {

  private final DenseDoubleVector initialProbability;
  private final DenseDoubleMatrix transitionProbabilities;

  public HMM(int numStates) {
    Preconditions.checkArgument(numStates > 1);
    initialProbability = new DenseDoubleVector(numStates, 1.0d / numStates);
    transitionProbabilities = new DenseDoubleMatrix(numStates, numStates,
        1.0d / numStates);
  }

  public double getProbability(Enum<?>[] observationSequence) {
    return Double.NaN;
  }

  public void trainBaumWelch(List<DenseDoubleVector> observations) {
    
  }

}
