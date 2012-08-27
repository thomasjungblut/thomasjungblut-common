package de.jungblut.datastructure;

import java.util.Random;

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

  @Test
  public void testQuickSelect() {
    // this checks the radix sort shortcut
    int[] array = new int[] { 1, 4, 3, 5, 2 };

    int first = ArrayUtils.quickSelect(ArrayUtils.copy(array), 1);
    assertEquals(0, first);

    int second = ArrayUtils.quickSelect(ArrayUtils.copy(array), 2);
    assertEquals(1, second);

    int third = ArrayUtils.quickSelect(ArrayUtils.copy(array), 3);
    assertEquals(2, third);

    int fourth = ArrayUtils.quickSelect(ArrayUtils.copy(array), 4);
    assertEquals(3, fourth);

    int fifth = ArrayUtils.quickSelect(ArrayUtils.copy(array), 5);
    assertEquals(4, fifth);

    // edge cases
    try {
      ArrayUtils.quickSelect(array, 0);
      fail();
    } catch (Exception e) {
      // should happen
    }

    try {
      ArrayUtils.quickSelect(array, 6);
      fail();
    } catch (Exception e) {
      // should happen
    }

    // this actually uses the real quickselect code without sort shortcuts
    array = new int[] { 1, 4, 3, 5, 2, 15, 23, 7, 6, 0, 19, 132 };
    first = ArrayUtils.quickSelect(array, 5);
    assertEquals(4, first);
  }

  @Test
  public void testMedianOfMedians() {
    int[] array = ArrayUtils.fromUpTo(0, 100, 1);
    int medianOfMedians = ArrayUtils.medianOfMedians(array);
    assertEquals(54, medianOfMedians);
  }

  @Test
  public void testFromUpTo() {
    int[] fromUpTo = ArrayUtils.fromUpTo(0, 100, 1);
    for (int i = 0; i < 100; i++) {
      assertEquals(i, fromUpTo[i]);
    }

    fromUpTo = ArrayUtils.fromUpTo(0, 100, 2);
    int index = 0;
    for (int i = 0; i < 100; i += 2) {
      assertEquals(i, fromUpTo[index++]);
    }

  }

  @Test
  public void testRadixSort() {
    int[] randomInput = getRandomInput(100, 1000);
    ArrayUtils.radixSort(randomInput);

    for (int i = 0; i < 99; i++) {
      assertTrue(randomInput[i] <= randomInput[i + 1]);
    }

  }

  @Test
  public void testCountingSort() {
    int[] randomInput = getRandomInput(100, 1000);
    ArrayUtils.countingSort(randomInput, 0, 1000);

    for (int i = 0; i < 99; i++) {
      assertTrue(randomInput[i] <= randomInput[i + 1]);
    }
  }

  @Test
  public void testQuickSort() {
    int[] randomInput = getRandomInput(100, 1000);
    ArrayUtils.quickSort(randomInput);

    for (int i = 0; i < 99; i++) {
      assertTrue(randomInput[i] <= randomInput[i + 1]);
    }
  }

  @Test
  public void testMultiQuickSort() {
    int[] first = new int[] { 10, 100, 100, 0 };
    int[] second = new int[] { 1, 3, 2, 4 };
    int[] resFirst = new int[] { 0, 10, 100, 100 };
    int[] resSecond = new int[] { 4, 1, 2, 3 };

    ArrayUtils.multiQuickSort(first, second);

    for (int i = 0; i < first.length; i++) {
      assertEquals(resFirst[i], first[i]);
      assertEquals(resSecond[i], second[i]);
    }

    // test the edge cases, all these should just be silently ignored and not
    // throw exceptions

    // empty
    ArrayUtils.multiQuickSort(new int[] {});

    // just single array
    ArrayUtils.multiQuickSort(new int[] { 2, 3, 4 });

    // negative sort index
    ArrayUtils.multiQuickSort(-1, new int[][] { { 1, 2, 3 }, { 1, 2, 3 } });

    // malformed length in one of the dimensions
    ArrayUtils.multiQuickSort(new int[][] { { 1, 2, 3 }, { 1, 2 } });

  }

  static int[] getRandomInput(int n, int k) {
    Random r = new Random();
    int[] arr = new int[n];
    for (int i = 0; i < n; i++) {
      arr[i] = r.nextInt(k);
    }
    return arr;
  }

  static void assertArrayEquals(int[] expected, int[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

}
