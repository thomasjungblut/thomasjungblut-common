package de.jungblut.classification.eval;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import de.jungblut.classification.ClassifierFactory;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.classification.knn.KNearestNeighbours;
import de.jungblut.math.DoubleVector;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.MushroomReader;

public class EvaluatorTest {

  private static final double EPS = 1e-3;

  @Test
  public void testEvaluationResult() throws Exception {

    // knn is stable with the mushroom data, so let's test it.
    Dataset dataset = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");
    EvaluationResult evaluation = Evaluator.evaluateClassifier(
        new KNearestNeighbours(2, 10), dataset.getFeatures(),
        dataset.getOutcomes(), 2, 0.99f, false);
    assertEquals(2, evaluation.getNumLabels());
    assertEquals(true, evaluation.isBinary());
    assertEquals(8042, evaluation.getTrainSize());
    assertEquals(82, evaluation.getTestSize());
    assertEquals(82, evaluation.getCorrect());
    assertEquals(1d, evaluation.getAccuracy(), EPS);
    assertEquals(45, evaluation.truePositive);
    assertEquals(0, evaluation.falseNegative);
    assertEquals(0, evaluation.falsePositive);
    assertEquals(37, evaluation.trueNegative);
  }

  @Test
  public void testCrossValidation() throws Exception {
    Dataset dataset = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");
    DoubleVector[] features = dataset.getFeatures();
    features = Arrays.copyOf(features, 500);
    DoubleVector[] outcome = dataset.getOutcomes();
    outcome = Arrays.copyOf(outcome, 500);

    EvaluationResult evaluation = Evaluator
        .<KNearestNeighbours> tenFoldCrossValidation(
            new ClassifierFactory<KNearestNeighbours>() {
              @Override
              public KNearestNeighbours newInstance() {
                return new KNearestNeighbours(2, 4);
              }
            }, features, outcome, 2, null, false);
    assertEquals(2, evaluation.getNumLabels());
    assertEquals(true, evaluation.isBinary());
    assertEquals(449, evaluation.getTrainSize());
    assertEquals(50, evaluation.getTestSize());

  }

}
