package de.jungblut.classification.knn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.classification.eval.Evaluator;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.MushroomReader;

public class KNearestNeighboursTest {

  @Test
  public void testKNN() throws Exception {
    Dataset mushroom = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");

    KNearestNeighbours knn = new KNearestNeighbours(2, 10);
    EvaluationResult res = Evaluator.evaluateClassifier(knn,
        mushroom.getFeatures(), mushroom.getOutcomes(), 2, 0.99f, false);

    // assert that everything is correct
    assertEquals(1d, res.getAccuracy(), 1e-5);

  }
}
