package de.jungblut.utils;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.hash.Hashing;

import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

public class VectorFunnelTest extends TestCase {

  @Test
  public void testDenseFunneling() {
    VectorFunnel funnel = new VectorFunnel();
    DenseDoubleVector vec = new DenseDoubleVector(new double[] { 1, 1 });
    long hash = Hashing.murmur3_128().newHasher().putObject(vec, funnel).hash()
        .asLong();
    assertEquals(1057272797145820176L, hash);
  }

  @Test
  public void testSparseFunneling() {
    VectorFunnel funnel = new VectorFunnel();
    SparseDoubleVector vec = new SparseDoubleVector(new double[] { 0d, 15, 25,
        0d, 255, 2, 20, 0d, 0d, 0d, 2 });
    long hash = Hashing.murmur3_128().newHasher().putObject(vec, funnel).hash()
        .asLong();
    assertEquals(-3943116774135188236L, hash);
  }

}
