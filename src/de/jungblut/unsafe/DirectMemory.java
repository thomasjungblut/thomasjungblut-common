package de.jungblut.unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

import sun.misc.Unsafe;

public class DirectMemory {

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
	int[] rand = getRandomIntArray(10);
	Arrays.sort(rand);
	System.out.println(Arrays.toString(rand));
	long mallocStart = unsafe.allocateMemory(rand.length * 4);
	long start = mallocStart;
	for (int i = 0; i < rand.length; i++) {
	    unsafe.putInt(start, rand[i]);
	    start += 4L;
	}
	
	int[] r = new int[rand.length];
	for (int i = 0; i < rand.length; i++) {
	    int ret = unsafe.getInt(mallocStart);
	    r[i] = ret;
	    mallocStart += 4L;
	}

	System.out.println(Arrays.toString(r));
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
