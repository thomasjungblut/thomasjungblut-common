package de.jungblut.classification;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.math.DoubleMath;

import de.jungblut.classification.Evaluator.EvaluationResult;
import de.jungblut.classification.knn.KNearestNeighbours;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.reader.MushroomReader;

public class EvaluatorTest extends TestCase {

  private static final double EPS = 1e-3;

  @Test
  public void testEvaluationResult() throws Exception {

    // knn is stable with the mushroom data, so let's test it.
    Tuple<DoubleVector[], DenseDoubleVector[]> readMushroomDataset = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");
    EvaluationResult evaluation = Evaluator.evaluateClassifier(
        new KNearestNeighbours(2, 10), readMushroomDataset.getFirst(),
        readMushroomDataset.getSecond(), 2, 0.99f, false);
    evaluation.print();
    assertEquals(2, evaluation.getNumLabels());
    assertEquals(true, evaluation.isBinary());
    assertEquals(8043, evaluation.getTrainSize());
    assertEquals(81, evaluation.getTestSize());
    assertEquals(81, evaluation.getCorrect());
    assertTrue(DoubleMath.fuzzyEquals(1d, evaluation.getAccuracy(), EPS));
    assertEquals(44, evaluation.truePositive);
    assertEquals(0, evaluation.falseNegative);
    assertEquals(0, evaluation.falsePositive);
    assertEquals(37, evaluation.trueNegative);
  }

  @Test
  public void testCrossValidation() throws Exception {
    Tuple<DoubleVector[], DenseDoubleVector[]> readMushroomDataset = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");

    EvaluationResult evaluation = Evaluator.tenFoldCrossValidation(
        new ClassifierFactory() {
          @Override
          public Classifier newInstance() {
            return new KNearestNeighbours(2, 4);
          }
        }, readMushroomDataset.getFirst(), readMushroomDataset.getSecond(), 2,
        null, false);
    assertEquals(2, evaluation.getNumLabels());
    assertEquals(true, evaluation.isBinary());
    assertEquals(7310, evaluation.getTrainSize());
    assertEquals(813, evaluation.getTestSize());
  }

}
