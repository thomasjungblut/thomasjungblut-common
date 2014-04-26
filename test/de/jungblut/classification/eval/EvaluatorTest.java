package de.jungblut.classification.eval;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import de.jungblut.classification.ClassifierFactory;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.classification.knn.KNearestNeighbours;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.MushroomReader;

public class EvaluatorTest {

  private static final double EPS = 1e-3;

  @Test(expected = IllegalAccessException.class)
  public void testAccessError() throws Exception {
    Evaluator.class.newInstance();
  }

  @Test
  public void testEvaluationResult() throws Exception {

    // knn is stable with the mushroom data, so let's test it.
    Dataset dataset = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");
    EvaluationResult evaluation = Evaluator.evaluateClassifier(
        new KNearestNeighbours(2, 10), dataset.getFeatures(),
        dataset.getOutcomes(), 0.99f, false);
    assertEquals(2, evaluation.getNumLabels());
    assertEquals(true, evaluation.isBinary());
    assertEquals(82, evaluation.getTestSize());
    assertEquals(82, evaluation.getCorrect());
    assertEquals(1d, evaluation.getAccuracy(), EPS);
    assertEquals(45, evaluation.getTruePositive());
    assertEquals(0, evaluation.getFalseNegative());
    assertEquals(0, evaluation.getFalsePositive());
    assertEquals(37, evaluation.getTrueNegative());
    assertEquals(1d, evaluation.getAUC(), EPS);
    assertEquals(1d, evaluation.getRecall(), EPS);
    assertEquals(1d, evaluation.getPrecision(), EPS);
    assertEquals(1d, evaluation.getF1Score(), EPS);
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
    assertEquals(50, evaluation.getTestSize());
  }

  @Test
  public void testAucComputationAllZeros() throws Exception {
    List<Tuple<Integer, Double>> outcomePredictedPairs = generateData(1000, 0.9);
    double aucValue = Evaluator.computeAUC(outcomePredictedPairs);
    assertEquals(0.5, aucValue, 1e-6);
  }

  @Test
  public void testAucComputationAllOnes() throws Exception {
    List<Tuple<Integer, Double>> outcomePredictedPairs = generateData(1000, 0d);
    double aucValue = Evaluator.computeAUC(outcomePredictedPairs);
    assertEquals(1, aucValue, 1e-6);
  }

  public List<Tuple<Integer, Double>> generateData(int numItems,
      double negativePercentage) {

    Random rand = new Random();
    List<Tuple<Integer, Double>> outcomePredictedPairs = new ArrayList<>(
        numItems);

    for (int i = 0; i < numItems; i++) {
      outcomePredictedPairs.add(new Tuple<>(
          rand.nextDouble() < negativePercentage ? 1 : 0, 0d));
    }
    return outcomePredictedPairs;
  }

}
