package de.jungblut.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;

public class CanopyClusteringTest extends TestCase {

  @Test
  public void testCanopyClustering() {

    double t1 = 100;
    double t2 = 50d;
    ArrayList<DoubleVector> input = KMeansClusteringTest.getClusteringInput();
    EuclidianDistance measure = new EuclidianDistance();
    List<DoubleVector> canopies = CanopyClustering.createCanopies(input,
        measure, t1, t2, false);

    assertEquals(8, canopies.size());
    // now check if the properties hold
    for (DoubleVector v : canopies) {
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
