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

}
