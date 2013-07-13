package de.jungblut.classification.eval;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.jungblut.math.DoubleVector;

@RunWith(JUnit4.class)
public class EvaluationSplitTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  // dummy arrays
  DoubleVector[] feats = new DoubleVector[100];
  DoubleVector[] outcome = new DoubleVector[100];

  @Test
  public void testNegativeSplitPercentage() throws Exception {
    exception.expect(IllegalArgumentException.class);
    EvaluationSplit.create(feats, outcome, -1f, false);
  }

  @Test
  public void testTooLargeSplitPercentage() throws Exception {
    exception.expect(IllegalArgumentException.class);
    EvaluationSplit.create(feats, outcome, 1.1f, false);
  }

  @Test
  public void testSplitting() {
    EvaluationSplit split = EvaluationSplit
        .create(feats, outcome, 0.87f, false);
    assertEquals(87, split.getTrainFeatures().length);
    assertEquals(13, split.getTestFeatures().length);
    assertEquals(87, split.getTrainOutcome().length);
    assertEquals(13, split.getTestOutcome().length);
  }

}
