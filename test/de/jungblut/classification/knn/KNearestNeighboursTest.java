package de.jungblut.classification.knn;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.jungblut.classification.eval.Evaluator;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.SingleEntryDoubleVector;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.MushroomReader;

public class KNearestNeighboursTest {

  @Test
  public void testKNN() throws Exception {
    Dataset mushroom = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");

    KNearestNeighbours knn = new KNearestNeighbours(2, 10);
    EvaluationResult res = Evaluator.evaluateClassifier(knn,
        mushroom.getFeatures(), mushroom.getOutcomes(), 0.99f, false);

    // assert that everything is correct
    assertEquals(1d, res.getAccuracy(), 1e-5);

  }

  @Test
  public void testMultiPrediction() {

    KNearestNeighbours knn = new KNearestNeighbours(5, 2);
    List<DoubleVector> features = new ArrayList<>();
    List<DoubleVector> outcome = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      features.add(new SingleEntryDoubleVector(i));
      double[] arr = new double[5];
      arr[i % 5] = 1d;
      outcome.add(new DenseDoubleVector(arr));
    }
    knn.train(features, outcome);

    DoubleVector prediction = knn.predict(new SingleEntryDoubleVector(5));
    assertArrayEquals(new double[] { 1d, 0, 0, 0, 1d }, prediction.toArray());
    prediction = knn.predictProbability(new SingleEntryDoubleVector(5));
    assertArrayEquals(new double[] { 0.5, 0, 0, 0, 0.5 }, prediction.toArray());
  }

  void assertArrayEquals(double[] real, double[] actual) {
    assertEquals(real.length, actual.length);
    for (int i = 0; i < real.length; i++) {
      assertEquals(real[i], actual[i], 1e-4);
    }
  }

}
