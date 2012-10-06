package de.jungblut.datastructure;

/**
 * Simple binary heap for integers.
 * 
 * @author thomas.jungblut
 * 
 */
public final class BinaryHeap {

  private static final int INITIAL_ITEMS = 10;

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
    heap = new int[INITIAL_ITEMS];
    minHeap = false;
  }

  /**
   * Constructs a heap with 10 elements.
   */
  public BinaryHeap(HeapType heapType) {
    heap = new int[INITIAL_ITEMS];
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

  /**
   * Adds an integer to the heap.
   */
  public void add(int a) {
    ensureSize(size + 1);
    heap[size] = a;
    upHeap(size);
    size++;
  }

  /**
   * Get's the head of the heap and removes it. If empty it will return
   * {@link Integer#MAX_VALUE} in case of a min heap or
   * {@link Integer#MIN_VALUE} in a max heap. So check the size before removing.
   */
  public int pop() {
    int head = heap[0];
    remove(0);

    return head;
  }

  /**
   * @return the current size of the heap
   */
  public int size() {
    return size;
  }

  // remove the index by setting it to max/min of the integer and down the heap.
  private void remove(int index) {
    heap[index] = minHeap ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    downHeap();
    size--;
  }

  // check if we exceeded our size yet
  private void ensureSize(int size) {
    if (size > heap.length) {
      grow();
    }
  }

  /**
   * Ups the heap index after an inserts.
   */
  private void upHeap(int pIndex) {
    int index = pIndex;
    boolean finished = false;
    while (!finished) {
      final int parentIndex = getIndexOfParent(index);
      final boolean greater = minHeap ? heap[index] < heap[parentIndex]
          : heap[index] > heap[parentIndex];
      if (greater) {
        ArrayUtils.swap(heap, index, parentIndex);
        index = parentIndex;
      } else {
        finished = true;
      }
      if (parentIndex == 0) {
        finished = true;
      }
    }
  }

  private int swapLowestWithRootAndRemove() {
    int root = heap[0];
    ArrayUtils.swap(heap, 0, size - 1);
    heap[size - 1] = minHeap ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    size--;
    downHeap();
    return root;
  }

  private void grow() {
    // we are growing with factor 2
    int[] temp = new int[heap.length * 2];
    System.arraycopy(heap, 0, temp, 0, heap.length);
    heap = temp;
  }

  private void downHeap() {
    int index = 0;
    boolean finished = false;
    while (!finished) {
      int children = getIndexOfLeftChild(index);
      if (children > heap.length)
        break;

      if (minHeap ? heap[children] > heap[children + 1]
          : heap[children] < heap[children + 1]) {
        children++;
      }

      if (minHeap ? heap[index] > heap[children] : heap[index] < heap[children]) {
        ArrayUtils.swap(heap, index, children);
        index = children;
      } else {
        finished = true;
      }

      if (children == size) {
        finished = true;
      }
    }
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
      }

      return temp;
    }

  }

}
