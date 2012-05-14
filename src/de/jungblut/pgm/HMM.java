package de.jungblut.pgm;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;

import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple3;

/**
 * A hidden markov model, fed by discrete values and trained by a Baum Welch
 * algorithm.
 */
public final class HMM {

  private final DenseDoubleVector initialProbability;
  private final DenseDoubleMatrix transitionProbabilities;

  private final HashMultiset<Enum<?>> countingSet;
  private final DenseDoubleVector discreteProbabilities;
  private final Enum<?>[] discreteValues;

  public HMM(Enum<?>[] discreteValues) {
    Preconditions.checkArgument(discreteValues.length > 1);
    int numStates = discreteValues.length;
    this.discreteValues = discreteValues;
    countingSet = HashMultiset.create(numStates);
    discreteProbabilities = new DenseDoubleVector(numStates);
    initialProbability = new DenseDoubleVector(numStates, 1.0d / numStates);
    transitionProbabilities = new DenseDoubleMatrix(numStates, numStates,
        1.0d / numStates);
  }

  public double getProbability(Enum<?>[] observationSequence) {
    return Double.NaN;
  }

  public void trainBaumWelch(List<Enum<?>[]> observations) {
    // gamma and xi arrays are those defined by Rabiner and Juang
    // allGamma[n] = gamma array associated to observation sequence n
    double allGamma[][][] = new double[observations.size()][][];

    /*
     * a[i][j] = aijNum[i][j] / aijDen[i] aijDen[i] = expected number of
     * transitions from state i aijNum[i][j] = expected number of transitions
     * from state i to j
     */
    double aijNum[][] = new double[initialProbability.getLength()][initialProbability
        .getLength()];
    double aijDen[] = new double[initialProbability.getLength()];

    int g = 0;
    // TODO rest ommitted

  }

  public Tuple3<double[][], double[][], Double> computeAlphaBetaAndProbability(
      Enum<?>[] seq) {
    double[][] alpha = new double[seq.length][initialProbability.getLength()];

    for (int i = 0; i < initialProbability.getLength(); i++) {
      // computeAlphaInit(hmm, oseq.get(0), i);
      // alpha[0][i] = hmm.getPi(i) * hmm.getOpdf(i).probability(o);
    }

    // TODO why is it starting by t=1 and not zero?
    for (int t = 1; t < seq.length; t++) {
      Enum<?> observation = seq[t];

      for (int i = 0; i < initialProbability.getLength(); i++) {
        // computeAlphaStep(hmm, observation, t, i);
        // double sum = 0.;
        // for (int i = 0; i < hmm.nbStates(); i++)
        // sum += alpha[t - 1][i] * hmm.getAij(i, j);
        // alpha[t][j] = sum * hmm.getOpdf(j).probability(o);
      }
    }

    // compute the backward step
    double[][] beta = new double[seq.length][initialProbability.getLength()];
    for (int i = 0; i < initialProbability.getLength(); i++) {
      beta[seq.length - 1][i] = 1.0;
    }
    for (int t = seq.length - 2; t >= 0; t--) {
      for (int i = 0; i < initialProbability.getLength(); i++) {
//        computeBetaStep(hmm, oseq.get(t + 1), t, i);
        // double sum = 0.;
        // for (int j = 0; j < initialProbability.getLength(); j++)
        // sum += beta[t + 1][j] * hmm.getAij(i, j)
        // * hmm.getOpdf(j).probability(o);
        // beta[t][i] = sum;
      }
    }

    return null;
  }

}
