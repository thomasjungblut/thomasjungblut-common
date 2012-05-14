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
    //gamma and xi arrays are those defined by Rabiner and Juang
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
    /*for (Enum<?>[] obsSeq : observations) {
      ForwardBackwardCalculator fbc = generateForwardBackwardCalculator(obsSeq,
          hmm);

      double xi[][][] = estimateXi(obsSeq, fbc, hmm);
      double gamma[][] = allGamma[g++] = estimateGamma(xi, fbc);

      for (int i = 0; i < initialProbability.getLength(); i++)
        for (int t = 0; t < obsSeq.length - 1; t++) {
          aijDen[i] += gamma[t][i];

          for (int j = 0; j < initialProbability.getLength(); j++)
            aijNum[i][j] += xi[t][i][j];
        }
    }
    /*
    for (int i = 0; i < initialProbability.getLength(); i++) {
      if (aijDen[i] == 0.) // State i is not reachable
        for (int j = 0; j < initialProbability.getLength(); j++)
          nhmm.setAij(i, j, hmm.getAij(i, j));
      else
        for (int j = 0; j < initialProbability.getLength(); j++)
          nhmm.setAij(i, j, aijNum[i][j] / aijDen[i]);
    }

    // pi computation
    for (int i = 0; i < initialProbability.getLength(); i++)
      nhmm.setPi(i, 0.);

    for (int o = 0; o < sequences.size(); o++)
      for (int i = 0; i < initialProbability.getLength(); i++)
        nhmm.setPi(i, nhmm.getPi(i) + allGamma[o][0][i] / sequences.size());

    // pdfs computation
    for (int i = 0; i < initialProbability.getLength(); i++) {
      List<O> observations = KMeansLearner.flat(sequences);
      double[] weights = new double[observations.size()];
      double sum = 0.;
      int j = 0;

      int o = 0;
      for (List<? extends O> obsSeq : sequences) {
        for (int t = 0; t < obsSeq.size(); t++, j++)
          sum += weights[j] = allGamma[o][t][i];
        o++;
      }

      for (j--; j >= 0; j--)
        weights[j] /= sum;

      Opdf<O> opdf = nhmm.getOpdf(i);
      opdf.fit(observations, weights);
    }*/
  }
  
  public Tuple3<double[][], double[][], Double> computeAlphaBetaAndProbability(){
    return null;
  }
  
  

}
