package de.jungblut.classification.tree;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    EvaluationResult res = Evaluator.testClassifier(forest,
        mushroom.getFeatures(), mushroom.getOutcomes());

    assertEquals(1d, res.getAccuracy(), 0.1);

  }

  @Test
  public void testSerialization() throws IOException {
    RandomForest forest = RandomForest.create(10).setNumRandomFeaturesToChoose(
        10);
    forest.train(mushroom.getFeatures(), mushroom.getOutcomes());
    EvaluationResult res = Evaluator.testClassifier(forest,
        mushroom.getFeatures(), mushroom.getOutcomes());
    assertEquals(1d, res.getAccuracy(), 0.1);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RandomForest.serialize(forest, new DataOutputStream(baos));
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    RandomForest deserialized = RandomForest.deserialize(new DataInputStream(
        bais));
    res = Evaluator.testClassifier(deserialized, mushroom.getFeatures(),
        mushroom.getOutcomes());
    assertEquals(1d, res.getAccuracy(), 0.1);

  }

}
