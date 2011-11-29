package de.jungblut.unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

import sun.misc.Unsafe;

public final class DirectMemory {

    public static void main(String[] args) {
	Unsafe unsafe = null;

	try {
	    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
	    field.setAccessible(true);
	    unsafe = (sun.misc.Unsafe) field.get(null);
	} catch (Exception e) {
	    throw new AssertionError(e);
	}

	// int = 32bit = 4bytes
	int[] rand = getRandomIntArray(1000000);
	Arrays.sort(rand);
	long mallocStart = unsafe.allocateMemory(rand.length * 4);
	long start = mallocStart;
	for (int i = 0; i < rand.length; i++) {
	    unsafe.putInt(start, rand[i]);
	    start += 4L;
	}

    }

    public static final int[] reconstructFromUnsafe(long mallocStart,
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


    public static final int[] getRandomIntArray(int size) {
	Random r = new Random();
	int[] arr = new int[size];
	for (int i = 0; i < size; i++) {
	    arr[i] = r.nextInt();
	}

	return arr;
    }

}
