package de.jungblut.classification.eval;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

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

  @Test
  public void testStratifiedSplits_50_50() {

    for (int i = 0; i < outcome.length; i++) {
      outcome[i] = new DenseDoubleVector(1);
      outcome[i].set(0, i % 2 == 0 ? 1d : 0d);
    }
    EvaluationSplit split = EvaluationSplit.createStratified(feats, outcome,
        0.5f, false);

    assertEquals(50, split.getTrainFeatures().length);
    assertEquals(50, split.getTestFeatures().length);
    assertEquals(50, split.getTrainOutcome().length);
    assertEquals(50, split.getTestOutcome().length);

    assertEquals(25, countPositives(split.getTrainOutcome()));
    assertEquals(25, countPositives(split.getTestOutcome()));
  }

  @Test
  public void testStratifiedSplits_25_75() {

    for (int i = 0; i < outcome.length; i++) {
      outcome[i] = new DenseDoubleVector(1);
      outcome[i].set(0, i < 25 ? 1d : 0d);
    }
    EvaluationSplit split = EvaluationSplit.createStratified(feats, outcome,
        0.8f, false);

    assertEquals(80, split.getTrainFeatures().length);
    assertEquals(80, split.getTrainOutcome().length);

    assertEquals(20, split.getTestFeatures().length);
    assertEquals(20, split.getTestOutcome().length);

    assertEquals(20, countPositives(split.getTrainOutcome()));
    assertEquals(5, countPositives(split.getTestOutcome()));
  }

  @Test
  public void testStratifiedSplits_1_99() {
    for (int i = 0; i < outcome.length; i++) {
      outcome[i] = new DenseDoubleVector(1);
      outcome[i].set(0, i < 1 ? 1d : 0d);
    }
    exception.expect(IllegalArgumentException.class);
    EvaluationSplit.createStratified(feats, outcome, 0.5f, false);
  }

  @Test
  public void testStratifiedSplits_2_98() {
    for (int i = 0; i < outcome.length; i++) {
      outcome[i] = new DenseDoubleVector(1);
      outcome[i].set(0, i < 2 ? 1d : 0d);
    }
    EvaluationSplit split = EvaluationSplit.createStratified(feats, outcome,
        0.5f, false);

    assertEquals(50, split.getTrainFeatures().length);
    assertEquals(50, split.getTrainOutcome().length);

    assertEquals(50, split.getTestFeatures().length);
    assertEquals(50, split.getTestOutcome().length);

    assertEquals(1, countPositives(split.getTrainOutcome()));
    assertEquals(1, countPositives(split.getTestOutcome()));
  }

  public int countPositives(DoubleVector[] vecs) {
    int positiveTrainClass = 0;
    for (int i = 0; i < vecs.length; i++) {
      if (vecs[i].get(0) == 1d) {
        positiveTrainClass++;
      }
    }
    return positiveTrainClass;
  }

}
