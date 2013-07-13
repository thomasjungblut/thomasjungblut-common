package de.jungblut.nlp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.sparse.SparseDoubleVector;
import de.jungblut.nlp.MinHash.HashType;

public class MinHashTest {

  @Test
  public void testMinHashing() throws Exception {

    SparseDoubleVector vec1 = new SparseDoubleVector(5);
    vec1.set(0, 2d);
    vec1.set(3, 311d);
    vec1.set(4, 2d);
    SparseDoubleVector vec2 = new SparseDoubleVector(5);
    vec2.set(0, 2d);
    vec1.set(2, 2d);
    vec2.set(3, 311d);
    vec2.set(4, 2d);

    MinHash minHash = MinHash.create(4);

    int[] minHashVector = minHash.minHashVector(vec1);
    int[] minHashVector2 = minHash.minHashVector(vec2);

    // we just differ by a single place, so it should still be 1.0 similarity
    double similarity = minHash
        .measureSimilarity(minHashVector, minHashVector2);
    assertEquals(1.0d, similarity, 1e-5);
  }

  @Test
  public void testMinHashingMurmur() throws Exception {

    SparseDoubleVector vec1 = new SparseDoubleVector(5);
    vec1.set(0, 2d);
    vec1.set(3, 311d);
    vec1.set(4, 2d);
    SparseDoubleVector vec2 = new SparseDoubleVector(5);
    vec2.set(0, 2d);
    vec1.set(2, 2d);
    vec2.set(3, 311d);
    vec2.set(4, 2d);

    MinHash minHash = MinHash.create(4, HashType.MURMUR128);

    int[] minHashVector = minHash.minHashVector(vec1);
    int[] minHashVector2 = minHash.minHashVector(vec2);

    // we just differ by a single place, so it should still be 1.0 similarity
    double similarity = minHash
        .measureSimilarity(minHashVector, minHashVector2);
    assertEquals(1.0d, similarity, 1e-5);
  }

  @Test
  public void testMinHashingMd5() throws Exception {

    SparseDoubleVector vec1 = new SparseDoubleVector(5);
    vec1.set(0, 2d);
    vec1.set(3, 311d);
    vec1.set(4, 2d);
    SparseDoubleVector vec2 = new SparseDoubleVector(5);
    vec2.set(0, 2d);
    vec1.set(2, 2d);
    vec2.set(3, 311d);
    vec2.set(4, 2d);

    MinHash minHash = MinHash.create(4, HashType.MD5);

    int[] minHashVector = minHash.minHashVector(vec1);
    int[] minHashVector2 = minHash.minHashVector(vec2);

    // we just differ by a single place, so it should still be 1.0 similarity
    double similarity = minHash
        .measureSimilarity(minHashVector, minHashVector2);
    assertEquals(1.0d, similarity, 1e-5);
  }

}
