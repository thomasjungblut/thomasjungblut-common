package de.jungblut.datastructure;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

public class KDTreeTest extends TestCase {

  @Test
  public void testInsert() throws Exception {
    KDTree tree = new KDTree();
    DenseDoubleVector[] array = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 2, 3 }),
        new DenseDoubleVector(new double[] { 5, 4 }),
        new DenseDoubleVector(new double[] { 9, 6 }),
        new DenseDoubleVector(new double[] { 4, 7 }),
        new DenseDoubleVector(new double[] { 8, 1 }),
        new DenseDoubleVector(new double[] { 7, 2 }), };

    for (DenseDoubleVector v : array)
      tree.add(v);

    int index = 0;
    Iterator<DoubleVector> iterator = tree.iterator();
    while (iterator.hasNext()) {
      DoubleVector next = iterator.next();
      assertTrue(array[index++] == next);
    }
  }

  @Test
  public void testKNearestNeighbours() throws Exception {
    KDTree tree = new KDTree();
    DenseDoubleVector[] array = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 2, 3 }),
        new DenseDoubleVector(new double[] { 5, 4 }),
        new DenseDoubleVector(new double[] { 9, 6 }),
        new DenseDoubleVector(new double[] { 4, 7 }),
        new DenseDoubleVector(new double[] { 8, 1 }),
        new DenseDoubleVector(new double[] { 7, 2 }), };

    for (DenseDoubleVector v : array)
      tree.add(v);

    List<VectorDistanceTuple> nearestNeighbours = tree.getNearestNeighbours(
        new DenseDoubleVector(new double[] { 0, 0 }), 1,
        new EuclidianDistance());
    assertEquals(1, nearestNeighbours.size());
    assertTrue(array[0] == nearestNeighbours.get(0).getVector());
  }

  @Test
  public void testMedian() throws Exception {
    assertEquals(1, KDTree.median(new DenseDoubleVector(new double[] { 2, 3 })));
    assertEquals(0, KDTree.median(new DenseDoubleVector(new double[] { 9, 6 })));
    assertEquals(2,
        KDTree.median(new DenseDoubleVector(new double[] { 9, 6, 8 })));
    assertEquals(1,
        KDTree.median(new DenseDoubleVector(new double[] { 9, 8, 7 })));
    assertEquals(0,
        KDTree.median(new DenseDoubleVector(new double[] { 8, 9, 6 })));

    assertEquals(
        3,
        KDTree.median(new DenseDoubleVector(new double[] { 8, 9, 6, 19, 25, 2,
            3, 4 })));
  }

  /*
   * sparse test vectors
   */

  @Test
  public void testInsertSparse() throws Exception {
    KDTree tree = new KDTree();
    DoubleVector[] array = new DoubleVector[] {
        new SparseDoubleVector(new double[] { 2, 3 }),
        new SparseDoubleVector(new double[] { 5, 4 }),
        new SparseDoubleVector(new double[] { 9, 6 }),
        new SparseDoubleVector(new double[] { 4, 7 }),
        new SparseDoubleVector(new double[] { 8, 1 }),
        new SparseDoubleVector(new double[] { 7, 2 }), };

    for (DoubleVector v : array)
      tree.add(v);

    int index = 0;
    Iterator<DoubleVector> iterator = tree.iterator();
    while (iterator.hasNext()) {
      DoubleVector next = iterator.next();
      assertTrue(array[index++] == next);
    }
  }

  @Test
  public void testKNearestNeighboursSparse() throws Exception {
    KDTree tree = new KDTree();
    DoubleVector[] array = new DoubleVector[] {
        new SparseDoubleVector(new double[] { 2, 3 }),
        new SparseDoubleVector(new double[] { 5, 4 }),
        new SparseDoubleVector(new double[] { 9, 6 }),
        new SparseDoubleVector(new double[] { 4, 7 }),
        new SparseDoubleVector(new double[] { 8, 1 }),
        new SparseDoubleVector(new double[] { 7, 2 }), };

    for (DoubleVector v : array)
      tree.add(v);

    List<VectorDistanceTuple> nearestNeighbours = tree.getNearestNeighbours(
        new SparseDoubleVector(new double[] { 0, 0 }), 1,
        new EuclidianDistance());
    assertEquals(1, nearestNeighbours.size());
    assertTrue(array[0] == nearestNeighbours.get(0).getVector());
  }

  @Test
  public void testMedianSparse() throws Exception {
    assertEquals(1,
        KDTree.median(new SparseDoubleVector(new double[] { 2, 3 })));
    assertEquals(0,
        KDTree.median(new SparseDoubleVector(new double[] { 9, 6 })));
    assertEquals(2,
        KDTree.median(new SparseDoubleVector(new double[] { 9, 6, 8 })));
    assertEquals(1,
        KDTree.median(new SparseDoubleVector(new double[] { 9, 8, 7 })));
    assertEquals(0,
        KDTree.median(new SparseDoubleVector(new double[] { 8, 9, 6 })));

    assertEquals(
        7,
        KDTree.median(new SparseDoubleVector(new double[] { 8, 9, 6, 19, 25, 2,
            3, 4 })));
  }

}
