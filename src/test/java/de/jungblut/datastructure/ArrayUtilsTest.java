package de.jungblut.datastructure;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class ArrayUtilsTest {

    @Test(expected = IllegalAccessException.class)
    public void testAccessError() throws Exception {
        ArrayUtils.class.newInstance();
    }

    @Test
    public void testFind() {
        String[] terms = new String[]{"A", "B", "C", "D", "E", "F"};
        int find = ArrayUtils.find(terms, "C");
        assertEquals(2, find);
        find = ArrayUtils.find(terms, "lol");
        assertEquals(-1, find);
    }

    @Test
    public void testFindInt() {
        int[] terms = new int[]{1, 2, -3, 6, -1, 3};
        int find = ArrayUtils.find(terms, -3);
        assertEquals(2, find);
        find = ArrayUtils.find(terms, 8);
        assertEquals(-1, find);
    }

    @Test
    public void testFindLong() {
        long[] terms = new long[]{1, 2, -3, 6, -1, 3};
        int find = ArrayUtils.find(terms, -3);
        assertEquals(2, find);
        find = ArrayUtils.find(terms, 8);
        assertEquals(-1, find);
    }

    @Test
    public void testConcat() {
        int[] arr1 = new int[]{0, 1, 2, 3, 4, 5};
        int[] arr2 = new int[]{6, 7, 8, 9, 10};
        int[] concat = ArrayUtils.concat(arr1, arr2);
        for (int i = 0; i < 11; i++) {
            assertEquals(i, concat[i]);
        }
    }

    @Test
    public void testConcatString() {
        String[] arr1 = new String[]{"a", "b", "c", "d", "e", "f"};
        String[] arr2 = new String[]{"g", "h", "i", "j", "k"};
        String[] concat = ArrayUtils.concat(arr1, arr2);
        for (int i = 0; i < 11; i++) {
            String expected = ((char) (i + 'a')) + "";
            assertEquals(expected, concat[i]);
        }
    }

    @Test
    public void testCopyInt() {
        int[] toCopy = new int[]{1, 2, 3};
        int[] copy = ArrayUtils.copy(toCopy);
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, copy[i]);
        }
    }

    @Test
    public void testCopyLong() {
        long[] toCopy = new long[]{1, 2, 3};
        long[] copy = ArrayUtils.copy(toCopy);
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, copy[i]);
        }
    }

    @Test
    public void testCopyDouble() {
        double[] toCopy = new double[]{1, 2, 3};
        double[] copy = ArrayUtils.copy(toCopy);
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, copy[i], 1e-6);
        }
    }

    @Test
    public void testCopyString() {
        String[] toCopy = new String[]{"a", "b", "c"};
        String[] copy = ArrayUtils.copy(toCopy);
        for (int i = 0; i < 3; i++) {
            assertEquals(toCopy[i], copy[i]);
        }
    }

    @Test
    public void testSwapInt() {
        int[] arr = new int[]{1, 2};
        ArrayUtils.swap(arr, 0, 1);
        assertEquals(2, arr[0]);
        assertEquals(1, arr[1]);
    }

    @Test
    public void testSwapLong() {
        long[] arr = new long[]{1, 2};
        ArrayUtils.swap(arr, 0, 1);
        assertEquals(2, arr[0]);
        assertEquals(1, arr[1]);
    }

    @Test
    public void testSwapDouble() {
        double[] arr = new double[]{1, 2};
        ArrayUtils.swap(arr, 0, 1);
        assertEquals(2, arr[0], 1e-6);
        assertEquals(1, arr[1], 1e-6);
    }

    @Test
    public void testSwapBoolean() {
        boolean[] arr = new boolean[]{false, true};
        ArrayUtils.swap(arr, 0, 1);
        assertEquals(true, arr[0]);
        assertEquals(false, arr[1]);
    }

    @Test
    public void testSwapString() {
        String[] arr = new String[]{"a", "b"};
        ArrayUtils.swap(arr, 0, 1);
        assertEquals("b", arr[0]);
        assertEquals("a", arr[1]);
    }

    @Test
    public void testPartition() {
        int[] arr1 = new int[]{0, 4, 2, 31, 25, 1};
        int pivot = ArrayUtils.partition(arr1);

        assertEquals(1, pivot);
        assertArrayEquals(new int[]{0, 1, 2, 31, 25, 4}, arr1);
    }

    @Test
    public void testQuickSelect() {
        // this checks the radix sort shortcut
        int[] array = new int[]{1, 4, 3, 5, 2};

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
        array = new int[]{1, 4, 3, 5, 2, 15, 23, 7, 6, 0, 19, 132};
        first = ArrayUtils.quickSelect(array, 5);
        assertEquals(4, first);
    }

    @Test
    public void testMedianOfMedians() {
        int[] array = ArrayUtils.fromUpTo(0, 100, 1);
        int medianOfMedians = ArrayUtils.medianOfMedians(array);
        assertEquals(54, medianOfMedians);

        array = ArrayUtils.fromUpTo(0, 10, 1);
        medianOfMedians = ArrayUtils.medianOfMedians(array);
        assertEquals(5, medianOfMedians);

        array = ArrayUtils.fromUpTo(0, 14, 1);
        medianOfMedians = ArrayUtils.medianOfMedians(array);
        assertEquals(7, medianOfMedians);
    }

    @Test
    public void testFromUpToInt() {
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
    public void testFromUpToLong() {
        long[] fromUpTo = ArrayUtils.fromUpTo(0l, 100l, 1l);
        for (int i = 0; i < 100; i++) {
            assertEquals(i, fromUpTo[i]);
        }

        fromUpTo = ArrayUtils.fromUpTo(0l, 100l, 2l);
        int index = 0;
        for (int i = 0; i < 100; i += 2) {
            assertEquals(i, fromUpTo[index++]);
        }
    }

    @Test
    public void testFromUpToDouble() {
        double[] fromUpTo = ArrayUtils.fromUpTo(0d, 100d, 1d);
        for (int i = 0; i < 100; i++) {
            assertEquals(i, fromUpTo[i], 1e-6);
        }

        fromUpTo = ArrayUtils.fromUpTo(0d, 100d, 2d);
        int index = 0;
        for (int i = 0; i < 100; i += 2) {
            assertEquals(i, fromUpTo[index++], 1e-6);
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
    public void testQuickSortLong() {
        long[] randomInput = getRandomInputLongs(100);
        ArrayUtils.quickSort(randomInput);

        for (int i = 0; i < 99; i++) {
            assertTrue(randomInput[i] <= randomInput[i + 1]);
        }
    }

    @Test
    public void testQuickSortDouble() {
        double[] randomInput = getRandomInputDoubles(100);
        ArrayUtils.quickSort(randomInput);

        for (int i = 0; i < 99; i++) {
            assertTrue(randomInput[i] <= randomInput[i + 1]);
        }
    }

    @Test
    public void testQuickSortStrings() {
        String[] input = new String[]{"b", "a", "z", "c", "y", "d", "x", "zz",
                "asd"};
        ArrayUtils.quickSort(input);
        for (int i = 0; i < input.length - 1; i++) {
            assertTrue(input[i].compareTo(input[i + 1]) <= 0);
        }
    }

    @Test
    public void testMultiQuickSort() {
        int[] first = new int[]{10, 100, 100, 0};
        int[] second = new int[]{1, 3, 2, 4};
        int[] resFirst = new int[]{0, 10, 100, 100};
        int[] resSecond = new int[]{4, 1, 2, 3};

        ArrayUtils.multiQuickSort(first, second);

        for (int i = 0; i < first.length; i++) {
            assertEquals(resFirst[i], first[i]);
            assertEquals(resSecond[i], second[i]);
        }

        // test the edge cases, all these should just be silently ignored and not
        // throw exceptions

        // empty
        ArrayUtils.multiQuickSort(new int[]{});

        // just single array
        ArrayUtils.multiQuickSort(new int[]{2, 3, 4});

        // negative sort index
        ArrayUtils.multiQuickSort(-1, new int[][]{{1, 2, 3}, {1, 2, 3}});

        // malformed length in one of the dimensions
        ArrayUtils.multiQuickSort(new int[][]{{1, 2, 3}, {1, 2}});
    }

    @Test
    public void testDeduplicate() {
        int[] arr = new int[]{1, 2, 2, 3, 4, 5, 5};
        int[] res = new int[]{1, 2, 3, 4, 5};

        int[] deduplicate = ArrayUtils.deduplicate(arr);
        assertArrayEquals(res, deduplicate);
    }

    @Test
    public void testUnion() {
        int[] arr = new int[]{1, 2, 3};
        int[] arr2 = new int[]{2, 3};

        int[] union = ArrayUtils.union(arr, arr2);
        // make sure its ascending, so we can compare here
        Arrays.sort(union);
        assertArrayEquals(arr, union);
    }

    @Test
    public void testIntersection() {
        int[] arr = new int[]{1, 2, 3};
        int[] arr2 = new int[]{2, 3, 3, 3};

        int[] intersect = ArrayUtils.intersection(arr, arr2);
        assertArrayEquals(new int[]{2, 3}, intersect);
    }

    @Test
    public void testIntersectionUnsorted() {
        int[] arr = new int[]{3, 5, 6, 1, 2, 3, 2, 2};
        int[] arr2 = new int[]{3, 2, 3, 3};

        int[] intersect = ArrayUtils.intersectionUnsorted(arr, arr2);
        Arrays.sort(intersect);
        assertArrayEquals(new int[]{2, 3}, intersect);
    }

    @Test
    public void testMissingNumber() {
        int[] arr = new int[]{0, 1, 2, 3, 5};

        int missingNumber = ArrayUtils.missingNumber(arr);
        assertEquals(4, missingNumber);
    }

    @Test
    public void testMinInt() {
        int[] arr = new int[]{0, 1, 2, -3, 5};
        int min = ArrayUtils.min(arr);
        assertEquals(-3, min);
        min = ArrayUtils.minIndex(arr);
        assertEquals(3, min);
    }

    @Test
    public void testMinLong() {
        long[] arr = new long[]{0, 1, 2, -3, 5};
        long min = ArrayUtils.min(arr);
        assertEquals(-3, min);
        min = ArrayUtils.minIndex(arr);
        assertEquals(3, min);
    }

    @Test
    public void testMinDouble() {
        double[] arr = new double[]{0, 1, 2, -3, 5};
        double min = ArrayUtils.min(arr);
        assertEquals(-3, min, 1e-6);
        min = ArrayUtils.minIndex(arr);
        assertEquals(3, min, 1e-6);
    }

    @Test
    public void testMaxInt() {
        int[] arr = new int[]{0, 1, 2, 3, 5};
        int max = ArrayUtils.max(arr);
        assertEquals(5, max);
        max = ArrayUtils.maxIndex(arr);
        assertEquals(4, max);
    }

    @Test
    public void testMaxLong() {
        long[] arr = new long[]{0, 1, 2, 3, 5};
        long max = ArrayUtils.max(arr);
        assertEquals(5, max);
        max = ArrayUtils.maxIndex(arr);
        assertEquals(4, max);
    }

    @Test
    public void testMaxDouble() {
        double[] arr = new double[]{0, 1, 2, 3, 5};
        double max = ArrayUtils.max(arr);
        assertEquals(5, max, 1e-6);
        max = ArrayUtils.maxIndex(arr);
        assertEquals(4, max, 1e-6);
    }

    @Test
    public void testSubArray() {
        String[] arr = new String[]{"1, 2, 3", "4, 5, 6", "7, 8, 9", "10, 11, 12"};

        String[] subArray = ArrayUtils.subArray(arr, 0, 2);
        assertEquals(3, subArray.length);

        for (int i = 0; i < 3; i++) {
            assertTrue(arr[i] == subArray[i]);
        }

    }

    @Test
    public void testShuffle() {
        String[] arr = new String[]{"1, 2, 3", "4, 5, 6", "7, 8, 9", "10, 11, 12"};
        String[] outcome = new String[]{"10, 11, 12", "1, 2, 3", "4, 5, 6",
                "7, 8, 9"};

        String[] shuffled = ArrayUtils.shuffle(arr, new Random(0));
        assertEquals(arr.length, shuffled.length);

        for (int i = 0; i < arr.length; i++) {
            assertTrue(outcome[i] == shuffled[i]);
        }

    }

    @Test
    public void testMerge() {
        int[] arr = new int[]{1, 2, 5};
        int[] arr2 = new int[]{3, 5, 6, 7};
        int[] merge = ArrayUtils.merge(arr, arr2);
        int[] expected = new int[]{1, 2, 3, 5, 5, 6, 7};
        assertArrayEquals(expected, merge);
    }

    @Test
    public void testInplaceMerge() {
        int[] arr = new int[]{1, 2, 5};
        int[] arr2 = new int[]{3, 5, 6, 7};
        int[] concat = ArrayUtils.concat(arr, arr2);
        ArrayUtils.merge(concat, 0, 2, 6);
        int[] expected = new int[]{1, 2, 3, 5, 5, 6, 7};
        assertArrayEquals(expected, concat);

        concat = ArrayUtils.concat(arr2, arr);
        ArrayUtils.merge(concat, 0, 3, 6);
        expected = new int[]{1, 2, 3, 5, 5, 6, 7};
        assertArrayEquals(expected, concat);

        concat = ArrayUtils.concat(arr2, arr2);
        ArrayUtils.merge(concat, 0, 3, 7);
        expected = new int[]{3, 3, 5, 5, 6, 6, 7, 7};
        assertArrayEquals(expected, concat);

    }

    @Test
    public void testIsValidIndexInt() {
        int[] dummy = new int[3];
        assertTrue(ArrayUtils.isValidIndex(dummy, 0));
        assertTrue(ArrayUtils.isValidIndex(dummy, 1));
        assertTrue(ArrayUtils.isValidIndex(dummy, 2));
        assertFalse(ArrayUtils.isValidIndex(dummy, -1));
        assertFalse(ArrayUtils.isValidIndex(dummy, 4));
    }

    @Test
    public void testIsValidIndexLong() {
        long[] dummy = new long[3];
        assertTrue(ArrayUtils.isValidIndex(dummy, 0));
        assertTrue(ArrayUtils.isValidIndex(dummy, 1));
        assertTrue(ArrayUtils.isValidIndex(dummy, 2));
        assertFalse(ArrayUtils.isValidIndex(dummy, -1));
        assertFalse(ArrayUtils.isValidIndex(dummy, 4));
    }

    @Test
    public void testIsValidIndexFloat() {
        float[] dummy = new float[3];
        assertTrue(ArrayUtils.isValidIndex(dummy, 0));
        assertTrue(ArrayUtils.isValidIndex(dummy, 1));
        assertTrue(ArrayUtils.isValidIndex(dummy, 2));
        assertFalse(ArrayUtils.isValidIndex(dummy, -1));
        assertFalse(ArrayUtils.isValidIndex(dummy, 4));
    }

    @Test
    public void testIsValidIndexDouble() {
        double[] dummy = new double[3];
        assertTrue(ArrayUtils.isValidIndex(dummy, 0));
        assertTrue(ArrayUtils.isValidIndex(dummy, 1));
        assertTrue(ArrayUtils.isValidIndex(dummy, 2));
        assertFalse(ArrayUtils.isValidIndex(dummy, -1));
        assertFalse(ArrayUtils.isValidIndex(dummy, 4));
    }

    @Test
    public void testIsValidIndexByte() {
        byte[] dummy = new byte[3];
        assertTrue(ArrayUtils.isValidIndex(dummy, 0));
        assertTrue(ArrayUtils.isValidIndex(dummy, 1));
        assertTrue(ArrayUtils.isValidIndex(dummy, 2));
        assertFalse(ArrayUtils.isValidIndex(dummy, -1));
        assertFalse(ArrayUtils.isValidIndex(dummy, 4));
    }

    @Test
    public void testIsValidIndexBoolean() {
        boolean[] dummy = new boolean[3];
        assertTrue(ArrayUtils.isValidIndex(dummy, 0));
        assertTrue(ArrayUtils.isValidIndex(dummy, 1));
        assertTrue(ArrayUtils.isValidIndex(dummy, 2));
        assertFalse(ArrayUtils.isValidIndex(dummy, -1));
        assertFalse(ArrayUtils.isValidIndex(dummy, 4));
    }

    @Test
    public void testIsValidIndexString() {
        String[] dummy = new String[3];
        assertTrue(ArrayUtils.isValidIndex(dummy, 0));
        assertTrue(ArrayUtils.isValidIndex(dummy, 1));
        assertTrue(ArrayUtils.isValidIndex(dummy, 2));
        assertFalse(ArrayUtils.isValidIndex(dummy, -1));
        assertFalse(ArrayUtils.isValidIndex(dummy, 4));
    }

    static long[] getRandomInputLongs(int n) {
        Random r = new Random();
        long[] arr = new long[n];
        for (int i = 0; i < n; i++) {
            arr[i] = r.nextLong();
        }
        return arr;
    }

    static double[] getRandomInputDoubles(int n) {
        Random r = new Random();
        double[] arr = new double[n];
        for (int i = 0; i < n; i++) {
            arr[i] = r.nextDouble();
        }
        return arr;
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
