package de.jungblut.datastructure;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

public class KDTreeTest extends TestCase {

  @Test
  public void testInsert() throws Exception {
    KDTree<Object> tree = new KDTree<>();
    DenseDoubleVector[] array = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 2, 3 }),
        new DenseDoubleVector(new double[] { 5, 4 }),
        new DenseDoubleVector(new double[] { 9, 6 }),
        new DenseDoubleVector(new double[] { 4, 7 }),
        new DenseDoubleVector(new double[] { 8, 1 }),
        new DenseDoubleVector(new double[] { 7, 2 }), };

    for (DenseDoubleVector v : array)
      tree.add(v, null);

    DenseDoubleVector[] result = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 2, 3 }),
        new DenseDoubleVector(new double[] { 5, 4 }),
        new DenseDoubleVector(new double[] { 8, 1 }),
        new DenseDoubleVector(new double[] { 9, 6 }),
        new DenseDoubleVector(new double[] { 4, 7 }),
        new DenseDoubleVector(new double[] { 7, 2 }), };

    int index = 0;
    Iterator<DoubleVector> iterator = tree.iterator();
    while (iterator.hasNext()) {
      DoubleVector next = iterator.next();
      assertEquals(result[index++], next);
    }
    assertEquals(result.length, index);
  }

  @Test
  public void testKNearestNeighbours() throws Exception {
    KDTree<Object> tree = new KDTree<>();
    DenseDoubleVector[] array = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 2, 3 }),
        new DenseDoubleVector(new double[] { 5, 4 }),
        new DenseDoubleVector(new double[] { 9, 6 }),
        new DenseDoubleVector(new double[] { 4, 7 }),
        new DenseDoubleVector(new double[] { 8, 1 }),
        new DenseDoubleVector(new double[] { 7, 2 }), };

    for (DenseDoubleVector v : array)
      tree.add(v, null);

    List<VectorDistanceTuple<Object>> nearestNeighbours = tree
        .getNearestNeighbours(new DenseDoubleVector(new double[] { 0, 0 }), 1);
    assertEquals(1, nearestNeighbours.size());
    assertTrue(array[0] == nearestNeighbours.get(0).getVector());
  }

  @Test
  public void testMedian() throws Exception {
    assertEquals(1,
        KDTree.median(new DenseDoubleVector(new double[] { 2, 3 }), 0));
    assertEquals(0,
        KDTree.median(new DenseDoubleVector(new double[] { 9, 6 }), 0));
    assertEquals(2,
        KDTree.median(new DenseDoubleVector(new double[] { 9, 6, 8 }), 0));
    assertEquals(1,
        KDTree.median(new DenseDoubleVector(new double[] { 9, 8, 7 }), 0));
    assertEquals(0,
        KDTree.median(new DenseDoubleVector(new double[] { 8, 9, 6 }), 0));

    assertEquals(
        1,
        KDTree.median(new DenseDoubleVector(new double[] { 8, 9, 6, 19, 25, 2,
            3, 4 }), 8));
  }

  @Test
  public void testRangeQuery() throws Exception {
    DenseDoubleVector[] array = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 2 }),
        new DenseDoubleVector(new double[] { 3 }),
        new DenseDoubleVector(new double[] { 4 }),
        new DenseDoubleVector(new double[] { 5 }),
        new DenseDoubleVector(new double[] { 6 }),
        new DenseDoubleVector(new double[] { 8 }), };

    KDTree<Object> tree = new KDTree<>();
    for (DenseDoubleVector v : array)
      tree.add(v, null);

    List<DoubleVector> rangeQuery = tree.rangeQuery(new DenseDoubleVector(
        new double[] { 4 }), new DenseDoubleVector(new double[] { 8 }));
    assertEquals(4, rangeQuery.size());
    for (int i = 2; i < array.length; i++)
      assertEquals(array[i], rangeQuery.get(i - 2));

    array = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 2 }),
        new DenseDoubleVector(new double[] { 8 }),
        new DenseDoubleVector(new double[] { 4 }),
        new DenseDoubleVector(new double[] { 3 }),
        new DenseDoubleVector(new double[] { 6 }),
        new DenseDoubleVector(new double[] { 5 }) };

    tree = new KDTree<>();
    for (DenseDoubleVector v : array)
      tree.add(v, null);

    rangeQuery = tree.rangeQuery(new DenseDoubleVector(new double[] { 4 }),
        new DenseDoubleVector(new double[] { 8 }));
    assertEquals(4, rangeQuery.size());
  }

  @Test
  public void testStrictHigherLower() throws Exception {
    DenseDoubleVector lower = new DenseDoubleVector(new double[] { 2 });
    DenseDoubleVector current = new DenseDoubleVector(new double[] { 5 });
    DenseDoubleVector upper = new DenseDoubleVector(new double[] { 10 });

    assertTrue(KDTree.strictHigher(lower, current));
    assertTrue(KDTree.strictHigher(current, upper));
    assertTrue(KDTree.strictLower(upper, current));
    assertTrue(KDTree.strictLower(current, lower));

  }

  /*
   * sparse test vectors
   */

  @Test
  public void testInsertSparse() throws Exception {
    KDTree<Object> tree = new KDTree<>();
    DoubleVector[] array = new DoubleVector[] {
        new SparseDoubleVector(new double[] { 2, 3 }),
        new SparseDoubleVector(new double[] { 5, 4 }),
        new SparseDoubleVector(new double[] { 9, 6 }),
        new SparseDoubleVector(new double[] { 4, 7 }),
        new SparseDoubleVector(new double[] { 8, 1 }),
        new SparseDoubleVector(new double[] { 7, 2 }), };

    DoubleVector[] result = new DoubleVector[] {
        new SparseDoubleVector(new double[] { 2, 3 }),
        new SparseDoubleVector(new double[] { 5, 4 }),
        new SparseDoubleVector(new double[] { 8, 1 }),
        new SparseDoubleVector(new double[] { 9, 6 }),
        new SparseDoubleVector(new double[] { 4, 7 }),
        new SparseDoubleVector(new double[] { 7, 2 }), };

    for (DoubleVector v : array)
      tree.add(v, null);

    int index = 0;
    Iterator<DoubleVector> iterator = tree.iterator();
    while (iterator.hasNext()) {
      DoubleVector next = iterator.next();
      assertEquals(result[index++], next);
    }
    assertEquals(array.length, index);
  }

  @Test
  public void testKNearestNeighboursSparse() throws Exception {
    KDTree<Object> tree = new KDTree<>();
    DoubleVector[] array = new DoubleVector[] {
        new SparseDoubleVector(new double[] { 2, 3 }),
        new SparseDoubleVector(new double[] { 5, 4 }),
        new SparseDoubleVector(new double[] { 9, 6 }),
        new SparseDoubleVector(new double[] { 4, 7 }),
        new SparseDoubleVector(new double[] { 8, 1 }),
        new SparseDoubleVector(new double[] { 7, 2 }), };

    for (DoubleVector v : array)
      tree.add(v, null);

    List<VectorDistanceTuple<Object>> nearestNeighbours = tree
        .getNearestNeighbours(new SparseDoubleVector(new double[] { 0, 0 }), 1);
    assertEquals(1, nearestNeighbours.size());
    assertTrue(array[0] == nearestNeighbours.get(0).getVector());
  }

  @Test
  public void testMedianSparse() throws Exception {
    assertEquals(1,
        KDTree.median(new SparseDoubleVector(new double[] { 2, 3 }), 0));
    assertEquals(0,
        KDTree.median(new SparseDoubleVector(new double[] { 9, 6 }), 0));
    assertEquals(2,
        KDTree.median(new SparseDoubleVector(new double[] { 9, 6, 8 }), 0));
    assertEquals(1,
        KDTree.median(new SparseDoubleVector(new double[] { 9, 8, 7 }), 0));
    assertEquals(0,
        KDTree.median(new SparseDoubleVector(new double[] { 8, 9, 6 }), 0));

    assertEquals(
        7,
        KDTree.median(new SparseDoubleVector(new double[] { 8, 9, 6, 19, 25, 2,
            3, 4 }), 0));
  }

}
