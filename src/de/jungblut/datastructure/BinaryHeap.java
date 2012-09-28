package de.jungblut.datastructure;

/**
 * Simple binary heap for integers.
 * 
 * @author thomas.jungblut
 * 
 */
public final class BinaryHeap {

  static enum HeapType {
    MIN, MAX
  }

  private final boolean minHeap;

  private int[] heap;
  private int size = 0;

  /**
   * Constructs a max heap with 10 elements.
   */
  public BinaryHeap() {
    heap = new int[10];
    minHeap = false;
  }

  /**
   * Constructs a heap with 10 elements.
   */
  public BinaryHeap(HeapType heapType) {
    heap = new int[10];
    this.minHeap = HeapType.MIN == heapType ? true : false;
  }

  /**
   * Constructs a heap with the given number of elements.
   */
  public BinaryHeap(int initialSize, HeapType heapType) {
    heap = new int[initialSize];
    this.minHeap = HeapType.MIN == heapType ? true : false;
  }

  /**
   * Constructs a max heap with the given number of elements.
   */
  public BinaryHeap(int initialSize) {
    heap = new int[initialSize];
    minHeap = false;
  }

  public void add(int a) {
    if (size == 0) {
      heap[0] = a;
    } else {
      ensureSize(size + 1);
      heap[size] = a;
      upHeap(size);
    }

    size++;
  }

  public void upHeap(int pIndex) {
    int index = pIndex;
    boolean finished = false;
    while (!finished) {
      final int parentIndex = getIndexOfParent(index);
      final boolean greater = minHeap ? heap[index] < heap[parentIndex]
          : heap[index] > heap[parentIndex];
      if (greater) {
        swap(index, parentIndex);
        index = parentIndex;
      } else {
        finished = true;
      }
      if (parentIndex == 0) {
        finished = true;
      }
    }
  }

  public int size() {
    return size;
  }

  public int get(int index) {
    return heap[index];
  }

  private void swap(int index1, int index2) {
    int temp = heap[index1];
    heap[index1] = heap[index2];
    heap[index2] = temp;
  }

  private void ensureSize(int size) {
    if (size > heap.length) {
      grow();
    }
  }

  public void remove(int index) {
    heap[index] = 0;
    size--;
  }

  private int swapLowestWithRootAndRemove() {
    int root = heap[0];
    swap(0, size - 1);
    remove(size - 1);
    return root;
  }

  private void grow() {
    // we are growing with factor 2
    int[] temp = new int[heap.length * 2];
    System.arraycopy(heap, 0, temp, 0, heap.length);
    heap = temp;
  }

  private static int getIndexOfParent(int i) {
    return (int) Math.floor((i - 1) / 2);
  }

  private static int getIndexOfLeftChild(int i) {
    return 2 * i + 1;
  }

  @SuppressWarnings("unused")
  private static int getIndexOfRightChild(int i) {
    return getIndexOfLeftChild(i) + 1;
  }

  public final static class HeapSort {

    public static int[] sort(int[] arr) {
      int[] temp = new int[arr.length];
      BinaryHeap heap = new BinaryHeap();
      for (int i : arr) {
        heap.add(i);
      }

      for (int i = 0; i < arr.length; i++) {
        temp[arr.length - i - 1] = heap.swapLowestWithRootAndRemove();
        downHeap(heap);
      }

      return temp;
    }

    private static void downHeap(BinaryHeap heapClass) {
      int index = 0;
      boolean finished = false;
      while (!finished) {
        int children = BinaryHeap.getIndexOfLeftChild(index);

        if (heapClass.heap[children] < heapClass.heap[children + 1]) {
          children++;
        }

        if (heapClass.heap[index] < heapClass.heap[children]) {
          heapClass.swap(index, children);
          index = children;
        } else {
          finished = true;
        }

        if (children == heapClass.size) {
          finished = true;
        }
      }
    }

  }

}
