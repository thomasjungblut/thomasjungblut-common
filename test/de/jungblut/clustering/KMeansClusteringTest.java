package de.jungblut.clustering;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class KMeansClusteringTest extends TestCase {

  @Test
  public void testKMeansClustering() {
    ArrayList<DoubleVector> lst = getClusteringInput();

    KMeansClustering clusterer = new KMeansClustering(2, lst, false);
    EuclidianDistance dist = new EuclidianDistance();
    ArrayList<DoubleVector>[] assignments = clusterer.cluster(100, dist, 0.1d,
        false);
    DoubleVector[] centers = clusterer.getCenters();
    assertEquals(49.5, centers[0].get(0));
    assertEquals(24.5, centers[0].get(1));
    assertEquals(49.5, centers[1].get(0));
    assertEquals(74.5, centers[1].get(1));

    // the centers should partition the space in half
    assertEquals(2, assignments.length);
    assertEquals(5000, assignments[0].size());
    assertEquals(5000, assignments[1].size());
    // now verify the assignments
    for (DoubleVector v : assignments[0]) {
      double distRightCenter = dist.measureDistance(v, centers[0]);
      double distOtherCenter = dist.measureDistance(v, centers[1]);
      assertTrue(distRightCenter < distOtherCenter);
    }
    for (DoubleVector v : assignments[1]) {
      double distRightCenter = dist.measureDistance(v, centers[1]);
      double distOtherCenter = dist.measureDistance(v, centers[0]);
      assertTrue(distRightCenter < distOtherCenter);
    }
  }

  public static ArrayList<DoubleVector> getClusteringInput() {
    // we are "sampling" a 100x100 grid to a vector space and let's do some
    // clustering.
    ArrayList<DoubleVector> lst = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < 100; j++) {
        lst.add(new DenseDoubleVector(new double[] { i, j }));
      }
    }
    return lst;
  }

}
