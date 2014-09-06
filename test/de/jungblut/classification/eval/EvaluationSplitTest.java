package de.jungblut.classification.eval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

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

  @Test
  public void testMultiClassStratifiedSplits_5_20() {
    DoubleVector[] feats = new DoubleVector[20_000];
    DoubleVector[] outcome = new DoubleVector[20_000];

    for (int i = 0; i < outcome.length; i++) {
      outcome[i] = new SparseDoubleVector(20);
      int index = i % 20;
      outcome[i].set(index, 1d);
    }

    EvaluationSplit split = EvaluationSplit.createStratified(feats, outcome,
        0.95f, false);

    assertEquals(19000, split.getTrainFeatures().length);
    assertEquals(19000, split.getTrainOutcome().length);

    assertEquals(1000, split.getTestFeatures().length);
    assertEquals(1000, split.getTestOutcome().length);

    for (int i = 0; i < 20; i++) {
      assertEquals(950, countClass(split.getTrainOutcome(), i));
      assertEquals(50, countClass(split.getTestOutcome(), i));
    }
  }

  @Test
  public void testMultiClassStratifiedSplits_5_19997() {
    DoubleVector[] feats = new DoubleVector[19997];
    DoubleVector[] outcome = new DoubleVector[19997];

    for (int i = 0; i < outcome.length; i++) {
      outcome[i] = new SparseDoubleVector(20);
      int index = i % 20;
      outcome[i].set(index, 1d);
    }

    EvaluationSplit split = EvaluationSplit.createStratified(feats, outcome,
        0.95f, false);

    assertEquals(18980, split.getTrainFeatures().length);
    assertEquals(18980, split.getTrainOutcome().length);

    assertEquals(1017, split.getTestFeatures().length);
    assertEquals(1017, split.getTestOutcome().length);

    for (int i = 0; i < 20; i++) {
      assertEquals(949, countClass(split.getTrainOutcome(), i));
      int countClass = countClass(split.getTestOutcome(), i);
      assertTrue(
          "class size in test size didn't match expected size of either 50 or 51! Was: "
              + countClass, countClass == 50 || countClass == 51);
    }
  }

  public int countClass(DoubleVector[] vecs, int classIndex) {
    int positiveTrainClass = 0;
    for (int i = 0; i < vecs.length; i++) {
      if (vecs[i].maxIndex() == classIndex) {
        positiveTrainClass++;
      }
    }
    return positiveTrainClass;
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
