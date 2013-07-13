package de.jungblut.nlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.google.common.base.Optional;

public class MarkovChainTest {

  static List<int[]> trainingSet = new ArrayList<>();
  static {
    trainingSet.add(new int[] { 1, 2 });
    trainingSet.add(new int[] { 2, 3, 4, 1 });
    trainingSet.add(new int[] { 1, 2, 0 });
    trainingSet.add(new int[] { 1, 2, 4, 1 });
  }

  @Test
  public void testSequenceProbability() {

    MarkovChain chain = MarkovChain.create(5);
    chain.train(trainingSet);

    double p = chain.getProbabilityForSequence(new int[] { 1, 2, 1 });
    // this sequence is not very probable, because we have never seen the
    // transition 2-1, even if 1-2 is very probable
    assertTrue(0.01 < p);
    p = chain.getProbabilityForSequence(new int[] { 1, 2 });
    assertEquals(1d, p, 1e-4);
    p = chain.getProbabilityForSequence(new int[] { 3, 4 });
    assertEquals(1d, p, 1e-4);
    p = chain.getProbabilityForSequence(new int[] { 2, 3 });
    assertEquals(1d, p, 1e-4);
    p = chain.getProbabilityForSequence(new int[] { 2, 3, 3 });
    assertEquals(0.333d, p, 1e-3);
  }

  @Test
  public void testSequenceCompletion() {

    MarkovChain chain = MarkovChain.create(5);
    chain.train(trainingSet);
    Optional<Random> absent = Optional.absent();
    int[] completeStateSequence = chain.completeStateSequence(absent,
        new int[] { -1, 2 }, 0);
    // the most probable should obviously 1 at index zero
    assertEquals(1, completeStateSequence[0]);

    try {
      completeStateSequence = chain.completeStateSequence(absent,
          new int[] { -1, }, 0);
      fail("This should fail with IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // good guy
    }

    completeStateSequence = chain.completeStateSequence(absent, new int[] { 1,
        -1 }, 1);
    // the most probable should obviously 2 at index one
    assertEquals(2, completeStateSequence[1]);

    // test multiple missings
    completeStateSequence = chain.completeStateSequence(absent, new int[] { 1,
        -1, 2, -1, 4 }, 1, 3);
    // the most probable should obviously 2 at index one
    assertEquals(2, completeStateSequence[1]);
    // based on the 2 at index 2 it should predict 4 for index 3
    assertEquals(4, completeStateSequence[3]);
  }
}
