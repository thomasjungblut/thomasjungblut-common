package de.jungblut.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

public final class OffHeapMemory {

    public static void main(String[] args) {
        Unsafe unsafe;

        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (sun.misc.Unsafe) field.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        // int = 32bit = 4bytes
        int[] rand = getRandomIntArray(100000000);
        Arrays.sort(rand);
        long mallocStart = unsafe.allocateMemory(rand.length * 4);
        long start = mallocStart;
        for (int aRand1 : rand) {
            unsafe.putInt(start, aRand1);
            start += 4L;
        }

        long startTime = System.currentTimeMillis();
        for (int aRand : rand) {
            binarySearch(unsafe, mallocStart, rand.length, aRand);
        }
        System.out.println("Took " + (System.currentTimeMillis() - startTime)
                + "ms");
    }

    private static int binarySearch(Unsafe unsafe, long mallocStart,
                                    int size, int key) {
        int low = 0;
        int high = size - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final int midVal = unsafe.getInt(mallocStart + mid * 4L);
            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1); // key not found.
    }

    public static int[] reconstructFromUnsafe(long mallocStart,
                                              Unsafe unsafe, int length) {
        long start = mallocStart;
        int[] r = new int[length];
        for (int i = 0; i < length; i++) {
            int ret = unsafe.getInt(start);
            r[i] = ret;
            start += 4L;
        }
        return r;
    }

    private static int[] getRandomIntArray(int size) {
        Random r = new Random();
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            final int x = r.nextInt(Integer.MAX_VALUE);
            arr[i] = x;
        }

        return arr;
    }

}
