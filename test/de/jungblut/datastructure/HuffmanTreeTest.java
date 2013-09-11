package de.jungblut.datastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.HashMultiset;

import de.jungblut.math.sparse.SparseBitVector;

@RunWith(JUnit4.class)
public class HuffmanTreeTest {

  @Test
  public void testInserts() {
    HashMultiset<Integer> set = getTestSet();

    HuffmanTree<Integer> huffmanTree = new HuffmanTree<>();
    huffmanTree.addAll(set);

    assertEquals(4, huffmanTree.getCardinality());

    Map<Integer, SparseBitVector> codes = huffmanTree.getHuffmanCodes();
    assertEquals(6, codes.size());
    // 1={1, 3}, 2={1}, 3={1, 2}, 4={2}, 5={}, 6={0}
    // now assert the generated codes
    assertCodeEquals(new int[] { 0, 1, 0, 1 }, codes.get(1));
    assertCodeEquals(new int[] { 0, 1, 0, 0 }, codes.get(2));
    assertCodeEquals(new int[] { 0, 1, 1, 0 }, codes.get(3));
    assertCodeEquals(new int[] { 0, 0, 1, 0 }, codes.get(4));
    assertCodeEquals(new int[] { 0, 0, 0, 0 }, codes.get(5));
    assertCodeEquals(new int[] { 1, 0, 0, 0 }, codes.get(6));

    // test some decodes
    assertEquals(1, huffmanTree
        .decode(generateVector(new int[] { 0, 1, 0, 1 })).intValue());
    assertEquals(2, huffmanTree
        .decode(generateVector(new int[] { 0, 1, 0, 0 })).intValue());
    assertEquals(3, huffmanTree
        .decode(generateVector(new int[] { 0, 1, 1, 0 })).intValue());
    assertEquals(4, huffmanTree
        .decode(generateVector(new int[] { 0, 0, 1, 0 })).intValue());
    assertEquals(5, huffmanTree
        .decode(generateVector(new int[] { 0, 0, 0, 0 })).intValue());
    assertEquals(6, huffmanTree
        .decode(generateVector(new int[] { 1, 0, 0, 0 })).intValue());

  }

  public SparseBitVector generateVector(int[] array) {
    SparseBitVector vec = new SparseBitVector(array.length);

    for (int i = 0; i < array.length; i++) {
      if (array[i] != 0) {
        vec.set(i, 1d);
      }
    }

    return vec;
  }

  public void assertCodeEquals(int[] expectedCode, SparseBitVector vector) {
    assertEquals(expectedCode.length, vector.getDimension());
    for (int i = 0; i < expectedCode.length; i++) {
      if (expectedCode[i] != (int) vector.get(i)) {
        fail("Expected code didn't match the real code: "
            + Arrays.toString(expectedCode) + " vs. " + vector.toString());
      }
    }

  }

  public HashMultiset<Integer> getTestSet() {
    HashMultiset<Integer> set = HashMultiset.create();
    set.add(1, 5);
    set.add(2, 7);
    set.add(3, 10);
    set.add(4, 15);
    set.add(5, 20);
    set.add(6, 45);
    return set;
  }

}
