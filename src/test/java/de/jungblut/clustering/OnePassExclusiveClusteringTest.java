package de.jungblut.clustering;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;

public class OnePassExclusiveClusteringTest {

  @Test
  public void testClustering() {

    double t1 = 10;
    ArrayList<DoubleVector> input = KMeansClusteringTest.getClusteringInput();
    EuclidianDistance measure = new EuclidianDistance();
    OnePassExclusiveClustering clusterer = new OnePassExclusiveClustering(t1,
        Integer.MAX_VALUE, 1, false);
    List<DoubleVector> centers = clusterer.cluster(input, false);

    assertEquals(6377, centers.size());
    // now check if the properties hold
    for (DoubleVector v : centers) {
      // remove all vectors that are in t1
      Iterator<DoubleVector> iterator = input.iterator();
      while (iterator.hasNext()) {
        DoubleVector next = iterator.next();
        if (measure.measureDistance(v, next) < t1) {
          iterator.remove();
        }
      }
    }
    // so in the end, we should have an empty input
    assertEquals(0, input.size());

  }
}
