package de.jungblut.classification.knn;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.distance.ManhattanDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.reader.MushroomReader;

public class KNearestNeighboursTest extends TestCase {

  @Test
  public void testKNN() throws Exception {
    Tuple<DoubleVector[], DoubleVector[]> mushroom = MushroomReader
        .readMushroomDataset();
    DoubleVector[] fullFeatures = mushroom.getFirst();
    DoubleVector[] fullOutcome = mushroom.getSecond();

    DoubleVector[] testFeatures = ArrayUtils.subArray(fullFeatures,
        fullFeatures.length - 100, fullFeatures.length - 1);

    DoubleVector[] testOutcome = ArrayUtils.subArray(fullOutcome,
        fullOutcome.length - 100, fullOutcome.length - 1);

    DoubleVector[] trainFeatures = ArrayUtils.subArray(fullFeatures,
        fullFeatures.length - 100);

    DoubleVector[] trainOutcome = ArrayUtils.subArray(fullOutcome,
        fullOutcome.length - 100);

    KNearestNeighbours knn = new KNearestNeighbours(trainFeatures,
        trainOutcome, new ManhattanDistance(), 2);

    int correct = 0;
    for (int i = 0; i < testFeatures.length; i++) {
      int predict = knn.predict(testFeatures[i], 4);
      if (predict == ((int) testOutcome[i].get(0))) {
        correct++;
      }
    }
    // assert that everything is correct
    assertEquals(100, correct);

  }
}
