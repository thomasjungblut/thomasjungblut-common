package de.jungblut.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.random.Well1024a;
import org.junit.Test;

import com.google.common.math.DoubleMath;

import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class MeanShiftClusteringTest extends TestCase {

  @Test
  public void testKDLookup() {
    HashSet<DoubleVector> lefts = new HashSet<>();
    List<DoubleVector> points = drawTwoDistinctDistributions(lefts,
        System.currentTimeMillis());
    KDTree<Integer> kdTree = new KDTree<>();
    int index = 0;
    for (DoubleVector v : points) {
      kdTree.add(v, index++);
    }
    double maxRadius = new EuclidianDistance().measureDistance(
        new double[] { 250 }, new double[] { 351 });

    List<VectorDistanceTuple<Integer>> neighbours = kdTree
        .getNearestNeighbours(new DenseDoubleVector(new double[] { 250 }),
            maxRadius);
    for (VectorDistanceTuple<Integer> x : neighbours) {
      lefts.remove(x.getVector());
    }
    assertEquals(0, lefts.size());
  }

  @Test
  public void testMeanShiftClustering() {
    double h = 10;
    List<DoubleVector> centers = MeanShiftClustering.cluster(
        drawTwoDistinctDistributions(null, 5L), h, 50, 2000, false);
    assertEquals(2, centers.size());
    assertTrue(DoubleMath.fuzzyEquals(centers.get(0).get(0), 244, 5));
    assertTrue(DoubleMath.fuzzyEquals(centers.get(1).get(0), 742, 5));
  }

  public List<DoubleVector> drawTwoDistinctDistributions(
      HashSet<DoubleVector> leftDistribution, long seed) {
    List<DoubleVector> lst = new ArrayList<>(100);

    double mean1 = 250;
    double mean2 = 750;
    RandomDataImpl random = new RandomDataImpl(new Well1024a(seed));
    for (int i = 0; i < 50; i++) {
      double nextGaussian1 = random.nextGaussian(mean1, Math.sqrt(100));
      assertTrue(nextGaussian1 >= 150 && nextGaussian1 <= 350);
      double nextGaussian2 = random.nextGaussian(mean2, Math.sqrt(100));
      assertTrue(nextGaussian2 >= 650 && nextGaussian2 <= 850);
      DenseDoubleVector lef = new DenseDoubleVector(
          new double[] { nextGaussian1 });
      lst.add(lef);
      if (leftDistribution != null) {
        leftDistribution.add(lef);
      }
      lst.add(new DenseDoubleVector(new double[] { nextGaussian2 }));
    }

    return lst;
  }
}
