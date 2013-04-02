package de.jungblut.nlp;

import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.math.DoubleMath;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class HMMTest extends TestCase {

  @Test
  public void testSupervisedUmbrellaWorld() {
    HMM hmm = new HMM(2, 2);
    DoubleVector[] features = new DenseDoubleVector[1000];
    DenseDoubleVector[] outcome = new DenseDoubleVector[1000];
    // we have 70% chance of observing an umbrella
    Random r = new Random(0L);
    for (int i = 0; i < features.length; i++) {
      if (r.nextDouble() > 0.7) {
        // umbrella
        features[i] = new DenseDoubleVector(new double[] { 0d, 1d });
        // in 10% our case we add noisy data
        if (r.nextDouble() > 0.9) {
          outcome[i] = new DenseDoubleVector(new double[] { 0d, 1d });
        } else {
          outcome[i] = new DenseDoubleVector(new double[] { 1d, 0d });
        }
      } else {
        // no umbrella
        features[i] = new DenseDoubleVector(new double[] { 1d, 0d });
        outcome[i] = new DenseDoubleVector(new double[] { 0d, 1d });
      }
    }

    hmm.trainSupervised(features, outcome);
    // note that we added +1 smoothing, so we don't end up exactly with 0.7
    assertEquals(hmm.getHiddenPriorProbability().getLength(), 2);
    assertTrue(DoubleMath.fuzzyEquals(hmm.getHiddenPriorProbability().get(0),
        0.71, 0.1));
    assertTrue(DoubleMath.fuzzyEquals(hmm.getHiddenPriorProbability().get(1),
        0.29, 0.1));

    // the transition matrix is equally working
    assertEquals(hmm.getTransitionProbabilityMatrix().getRowCount(), 2);
    assertEquals(hmm.getTransitionProbabilityMatrix().getColumnCount(), 2);
    assertTrue(DoubleMath.fuzzyEquals(
        hmm.getTransitionProbabilityMatrix().get(0, 0), 0.71, 0.1));
    assertTrue(DoubleMath.fuzzyEquals(
        hmm.getTransitionProbabilityMatrix().get(0, 1), 0.28, 0.1));
    assertTrue(DoubleMath.fuzzyEquals(
        hmm.getTransitionProbabilityMatrix().get(1, 0), 0.72, 0.1));
    assertTrue(DoubleMath.fuzzyEquals(
        hmm.getTransitionProbabilityMatrix().get(1, 1), 0.28, 0.1));

    // the emission probability must look quite extreme as we only add 10% noise
    // and otherwise observe the same like the observation
    assertEquals(hmm.getEmissionProbabilitiyMatrix().getRowCount(), 2);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().getColumnCount(), 2);
    assertTrue(DoubleMath.fuzzyEquals(
        hmm.getEmissionProbabilitiyMatrix().get(0, 0), 0.99, 0.1));
    assertTrue(DoubleMath.fuzzyEquals(
        hmm.getEmissionProbabilitiyMatrix().get(0, 1), 0.01, 0.1));
    assertTrue(DoubleMath.fuzzyEquals(
        hmm.getEmissionProbabilitiyMatrix().get(1, 0), 0.08, 0.1));
    assertTrue(DoubleMath.fuzzyEquals(
        hmm.getEmissionProbabilitiyMatrix().get(1, 1), 0.91, 0.1));

  }
}
