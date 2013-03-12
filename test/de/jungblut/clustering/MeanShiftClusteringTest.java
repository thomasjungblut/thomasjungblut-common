package de.jungblut.clustering;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.math3.random.RandomDataImpl;
import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class MeanShiftClusteringTest extends TestCase {

  @Test
  public void testMeanShiftClustering() {
    double h = 450;
    List<DoubleVector> centers = MeanShiftClustering.cluster(
        drawTwoDistinctDistributions(), h, 100, true);

    System.out.println(centers);

  }

  public List<DoubleVector> drawTwoDistinctDistributions() {
    List<DoubleVector> lst = new ArrayList<>(100);

    double mean1 = 250;
    double mean2 = 750;
    RandomDataImpl random = new RandomDataImpl();
    for (int i = 0; i < 50; i++) {
      double nextGaussian1 = random.nextGaussian(mean1, 100);
      double nextGaussian2 = random.nextGaussian(mean2, 100);
      lst.add(new DenseDoubleVector(new double[] { nextGaussian1 }));
      lst.add(new DenseDoubleVector(new double[] { nextGaussian2 }));
    }

    return lst;
  }

}
