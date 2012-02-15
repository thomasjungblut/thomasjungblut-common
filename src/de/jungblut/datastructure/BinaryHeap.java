package de.jungblut.datastructure;

import java.util.Arrays;

public class BinaryHeap {

    private int[] heap;
    private int size = 0;

    private BinaryHeap() {
        heap = new int[10];
    }

    void add(int a) {
        if (size == 0) {
            heap[0] = a;
        } else {
            ensureSize(size + 1);
            heap[size] = a;
            upHeap(size);
        }

        size++;
    }

    void upHeap(int index) {
        boolean finished = false;
        while (!finished) {
            int parentIndex = getIndexOfParent(index);
            if (heap[index] > heap[parentIndex]) {
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

    private void remove(int index) {
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

    private int getIndexOfParent(int i) {
        return (int) Math.floor((i - 1) / 2);
    }

    private int getIndexOfLeftChild(int i) {
        return 2 * i + 1;
    }

    @SuppressWarnings("unused")
    private int getIndexOfRightChild(int i) {
        return getIndexOfLeftChild(i) + 1;
    }

    final static class HeapSort {

        public static int[] sort(int[] arr) {
            int[] temp = new int[arr.length];
            Arrays.fill(temp, 0);
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
                int children = heapClass.getIndexOfLeftChild(index);

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

    public static void main(String[] args) {

        int[] arr = {100, 12, 136, 15, 26, 723, 62, 6184, 8, 1, 3, 2, 72};
        System.out.println(Arrays.toString(HeapSort.sort(arr)));
    }

}
