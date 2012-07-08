package de.jungblut.datastructure;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.datastructure.BinaryHeap.HeapSort;

public class BinaryHeapTest extends TestCase {

  @Test
  public void testHeapSort() {

    int[] arr = { 100, 12, 136, 15, 26, 723, 62, 6184, 8, 1, 3, 2, 72 };
    int[] sort = HeapSort.sort(arr);

    int lastItem = 0;
    for (int i = 0; i < arr.length; i++) {
      assertTrue(sort[i] > lastItem);
      lastItem = sort[i];
    }

  }

}
