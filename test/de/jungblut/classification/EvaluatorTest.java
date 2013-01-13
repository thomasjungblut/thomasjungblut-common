package de.jungblut.classification;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.classification.Evaluator.EvaluationResult;
import de.jungblut.classification.knn.KNearestNeighbours;
import de.jungblut.distance.ManhattanDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.reader.MushroomReader;

public class EvaluatorTest extends TestCase {

  @Test
  public void testEvaluationResult() throws Exception {

    // knn is stable with the mushroom data, so let's test it.
    Tuple<DoubleVector[], DenseDoubleVector[]> readMushroomDataset = MushroomReader
        .readMushroomDataset();
    EvaluationResult evaluation = Evaluator.evaluateClassifier(
        new KNearestNeighbours(new ManhattanDistance(), 2, 4),
        readMushroomDataset.getFirst(), readMushroomDataset.getSecond(), 2,
        0.99f, false);

    assertEquals(2, evaluation.getNumLabels());
    assertEquals(true, evaluation.isBinary());
    assertEquals(8043, evaluation.getTrainSize());
    assertEquals(81, evaluation.getTestSize());
    assertEquals(81, evaluation.getCorrect());
    assertEquals(1.0d, evaluation.getAccuracy());
    assertEquals(44, evaluation.truePositive);
    assertEquals(0, evaluation.falseNegative);
    assertEquals(0, evaluation.falsePositive);
    assertEquals(37, evaluation.trueNegative);
    assertEquals(1.0d, evaluation.getPrecision());
  }

  @Test
  public void testCrossValidation() throws Exception {
    Tuple<DoubleVector[], DenseDoubleVector[]> readMushroomDataset = MushroomReader
        .readMushroomDataset();

    EvaluationResult evaluation = Evaluator.tenFoldCrossValidation(
        new ClassifierFactory() {
          @Override
          public Classifier newInstance() {
            return new KNearestNeighbours(new ManhattanDistance(), 2, 4);
          }
        }, readMushroomDataset.getFirst(), readMushroomDataset.getSecond(), 2,
        null, false);
    assertEquals(2, evaluation.getNumLabels());
    assertEquals(true, evaluation.isBinary());
    assertEquals(7310, evaluation.getTrainSize());
    assertEquals(813, evaluation.getTestSize());
  }

}
