package de.jungblut.math.squashing;

import org.junit.Assert;
import org.junit.Test;

import de.jungblut.math.sparse.SparseDoubleRowMatrix;

public class HammingLossFunctionTest {

  @Test
  public void testSingleHammingLoss() {
    SparseDoubleRowMatrix groundTruth = new SparseDoubleRowMatrix(
        new double[][] { { 1, 1, 1, 1 } });

    SparseDoubleRowMatrix hypo = new SparseDoubleRowMatrix(new double[][] { {
        0, 1, 1, 1 } });

    HammingLossFunction fnc = new HammingLossFunction(0.5d);

    double err = fnc.calculateError(groundTruth, hypo);
    Assert.assertEquals(0.25, err, 0.1d);

  }

  @Test
  public void testMultiHammingLoss() {
    SparseDoubleRowMatrix groundTruth = new SparseDoubleRowMatrix(
        new double[][] { { 1d, 0d }, { 0d, 1d } });

    SparseDoubleRowMatrix hypo = new SparseDoubleRowMatrix(new double[][] {
        { 1d, 0d }, { 0d, 0d } });

    HammingLossFunction fnc = new HammingLossFunction(0.5d);

    double err = fnc.calculateError(groundTruth, hypo);
    Assert.assertEquals(0.5, err, 0.1d);
  }

}
