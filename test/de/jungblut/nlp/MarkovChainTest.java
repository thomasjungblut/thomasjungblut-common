package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.sparse.SparseDoubleColumnMatrix;

public class MarkovChainTest extends TestCase {

  static List<int[]> trainingSet = new ArrayList<>();
  static {
    trainingSet.add(new int[] { 1, 2 });
    trainingSet.add(new int[] { 2, 3, 4, 1 });
    trainingSet.add(new int[] { 1, 2, 0 });
    trainingSet.add(new int[] { 1, 2, 4, 1 });
  }

  static double[][] result = new double[][] { { 0, 0, 0, 0, 0 },
      { 0, 0, 3, 0, 0 },
      { -1.0986122886681098, 0, 0, -1.0986122886681098, -1.0986122886681098 },
      { 0, 0, 0, 0, 1 }, { 0, 2, 0, 0, 0 } };

  @Test
  public void testProbabilityMatrix() {

    MarkovChain chain = MarkovChain.create(5);
    chain.train(trainingSet);

    SparseDoubleColumnMatrix mat = chain.getTransitionProbabilities();

    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        // note that this is inverted here
        assertEquals(result[i][j], mat.get(j, i));
      }
    }
  }

  @Test
  public void testSequenceProbability() {

    MarkovChain chain = MarkovChain.create(5);
    chain.train(trainingSet);

    double p = chain.getProbabilityForSequence(new int[] { 1, 2, 1 });
    // this sequence is not very probable, because we have never seen the
    // transition 2-1, even if 1-2 is very probable
    assertTrue(0.03 < p && p < 0.05);
    p = chain.getProbabilityForSequence(new int[] { 1, 2 });
    assertEquals(1d, p);

  }

  @Test
  public void testSequenceCompletion() {

    MarkovChain chain = MarkovChain.create(5);
    chain.train(trainingSet);

    int[] completeStateSequence = chain.completeStateSequence(
        new int[] { -1, 2 }, 0);
    // the most probable should obviously 1 at index zero
    assertEquals(1, completeStateSequence[0]);

    try {
      completeStateSequence = chain.completeStateSequence(new int[] { -1, }, 0);
      fail("This should fail with IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // good guy
    }

    completeStateSequence = chain.completeStateSequence(new int[] { 1, -1 }, 1);
    // the most probable should obviously 2 at index one
    assertEquals(2, completeStateSequence[1]);

    // test multiple missings
    completeStateSequence = chain.completeStateSequence(new int[] { 1, -1, 2,
        -1, 4 }, 1, 3);
    // the most probable should obviously 2 at index one
    assertEquals(2, completeStateSequence[1]);
    // based on the 2 at index 2 it should predict 4 for index 3
    assertEquals(4, completeStateSequence[3]);
  }
}
