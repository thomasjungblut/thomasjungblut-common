package de.jungblut.sort;

import java.util.Arrays;

public class CountingSort {

    private static int[] sort(int[] a, int low, int high) {
        int[] counts = new int[high - low + 1];
        for (int x : a)
            counts[x - low]++;
        int current = 0;
        for (int i = 0; i < counts.length; i++) {
            Arrays.fill(a, current, current + counts[i], i + low);
            current += counts[i]; // leap forward by counts[i] steps
        }
        return a;
    }

    public static void main(String[] args) {
        int[] x = new int[]{25, 67, 123, 213, 192, 1, 0};
        System.out.println(Arrays.toString(sort(x, 0, 213)));
    }

}
