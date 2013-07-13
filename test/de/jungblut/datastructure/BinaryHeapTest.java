package de.jungblut.datastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.jungblut.datastructure.BinaryHeap.HeapSort;
import de.jungblut.datastructure.BinaryHeap.HeapType;

public class BinaryHeapTest {

  @Test
  public void testHeapInsert() {
    // max heap
    BinaryHeap heap = new BinaryHeap();
    fillHeap(heap);

    assertEquals(6184, heap.pop());
    assertEquals(12, heap.size());
    assertEquals(723, heap.pop());
    assertEquals(11, heap.size());

    // min heap
    heap = new BinaryHeap(HeapType.MIN);
    fillHeap(heap);

    assertEquals(1, heap.pop());
    assertEquals(12, heap.size());
    assertEquals(2, heap.pop());
    assertEquals(11, heap.size());

  }

  private void fillHeap(BinaryHeap heap) {
    int[] arr = { 100, 12, 136, 15, 26, 723, 62, 6184, 8, 1, 3, 2, 72 };
    for (int a : arr)
      heap.add(a);
  }

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
