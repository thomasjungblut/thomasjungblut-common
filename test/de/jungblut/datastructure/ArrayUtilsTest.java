package de.jungblut.datastructure;

import junit.framework.TestCase;

import org.junit.Test;

public class ArrayUtilsTest extends TestCase {

  @Test
  public void testFind() {
    String[] terms = new String[] { "A", "B", "C", "D", "E", "F" };
    int find = ArrayUtils.find(terms, "C");
    assertEquals(2, find);
    find = ArrayUtils.find(terms, "lol");
    assertEquals(-1, find);
  }

  @Test
  public void testConcat() {
    int[] arr1 = new int[] { 0, 1, 2, 3, 4, 5 };
    int[] arr2 = new int[] { 6, 7, 8, 9, 10 };
    int[] concat = ArrayUtils.concat(arr1, arr2);
    for (int i = 0; i < 11; i++) {
      assertEquals(i, concat[i]);
    }
  }

  @Test
  public void testPartition() {
    int[] arr1 = new int[] { 0, 4, 2, 31, 25, 1 };
    int pivot = ArrayUtils.partition(arr1);

    assertEquals(1, pivot);
    assertArrayEquals(new int[] { 0, 1, 2, 31, 25, 4 }, arr1);
  }

  static void assertArrayEquals(int[] expected, int[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

}
