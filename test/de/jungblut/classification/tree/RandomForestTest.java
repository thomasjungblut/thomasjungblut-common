package de.jungblut.classification.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.classification.eval.Evaluator;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.MushroomReader;

public class RandomForestTest {

  static Dataset mushroom = MushroomReader
      .readMushroomDataset("files/mushroom/mushroom_dataset.csv");

  // simple end2end test of the random forest

  @Test
  public void testRandomForest() {

    RandomForest forest = RandomForest.create(10).setNumRandomFeaturesToChoose(
        10);
    forest.train(mushroom.getFeatures(), mushroom.getOutcomes());

    EvaluationResult res = Evaluator.testClassifier(forest, 2, null,
        mushroom.getFeatures().length, mushroom.getFeatures(),
        mushroom.getOutcomes());

    assertEquals(1d, res.getAccuracy(), 0.1);

  }

}
