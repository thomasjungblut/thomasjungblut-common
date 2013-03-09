package de.jungblut.clustering;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class DBSCANClusteringTest extends TestCase {

  @Test
  public void testDBSCAN() {
    List<DoubleVector> input = KMeansClusteringTest.getClusteringInput();
    // add some noise!
    input.add(new DenseDoubleVector(new double[] { 2000, 2000 }));

    EuclidianDistance measure = new EuclidianDistance();

    List<List<DoubleVector>> cluster = DBSCANClustering.cluster(input, measure,
        5, 100d);
    // we should just have a single connected cluster with all the points, but
    // the noise
    assertEquals(1, cluster.size());
    assertEquals(input.size() - 1, cluster.get(0).size());

    // check the noise
    List<DoubleVector> noise = DBSCANClustering.findNoise(input, cluster);
    assertEquals(1, noise.size());
    assertEquals(noise.get(0), input.get(input.size() - 1));
  }

}
