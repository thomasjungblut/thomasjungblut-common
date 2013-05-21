package de.jungblut.classification.knn;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.distance.CosineDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

public class SparseKNearestNeighboursTest extends TestCase {

  @Test
  public void testSparseKNN() {

    SparseKNearestNeighbours neighbours = new SparseKNearestNeighbours(2, 2,
        new CosineDistance());

    // we seperate stuff in two dimensions each
    DoubleVector left = new DenseDoubleVector(new double[] { 0d });
    DoubleVector right = new DenseDoubleVector(new double[] { 1d });
    DoubleVector v1 = new SparseDoubleVector(4);
    v1.set(0, 1d);
    v1.set(1, 1d);

    DoubleVector v2 = new SparseDoubleVector(4);
    v2.set(2, 1d);
    v2.set(3, 2.5);

    DoubleVector v3 = new SparseDoubleVector(4);
    v3.set(0, 2d);
    v3.set(1, 2d);

    DoubleVector v4 = new SparseDoubleVector(4);
    v4.set(2, 0.5);
    v4.set(3, 1.5);

    DoubleVector[] trainingSet = new DoubleVector[] { v1, v2, v3, v4 };
    DoubleVector[] outcomeSet = new DoubleVector[] { left, right, left, right };

    neighbours.train(trainingSet, outcomeSet);

    DoubleVector predict = neighbours.predict(v4);
    assertEquals(right, predict);

    predict = neighbours.predict(v2);
    assertEquals(right, predict);

    predict = neighbours.predict(v1);
    assertEquals(left, predict);

    predict = neighbours.predict(v3);
    assertEquals(left, predict);

    // predict between, slightly to the right
    DoubleVector vx = new SparseDoubleVector(4);
    vx.set(1, 1d);
    vx.set(3, 2.5);

    predict = neighbours.predict(vx);
    assertEquals(right, predict);
  }

}
