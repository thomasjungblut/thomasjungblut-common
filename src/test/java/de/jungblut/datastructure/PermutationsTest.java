package de.jungblut.datastructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.junit.Assert;
import org.junit.Test;

public class PermutationsTest {

    @Test
    public void testOneElementPermutation() {
        Permutations<Integer> perm = new Permutations<>(new Integer[]{1});
        List<Integer[]> allPermutations = allPermutations(perm, 1);
        assertEquals(allPermutations, new Integer[][]{{1}});
        Assert.assertNull(perm.nextPermutation());
    }

    @Test
    public void testTwoElementPermutation() {
        Permutations<Integer> perm = new Permutations<>(new Integer[]{1, 2});
        List<Integer[]> allPermutations = allPermutations(perm, 2);
        assertEquals(allPermutations, new Integer[][]{{1, 2}, {2, 1}});
        Assert.assertNull(perm.nextPermutation());
    }

    @Test
    public void testThreeElementPermutation() {
        Permutations<Integer> perm = new Permutations<>(new Integer[]{1, 2, 3});
        List<Integer[]> allPermutations = allPermutations(perm, 3);
        assertEquals(allPermutations, new Integer[][]{{1, 2, 3}, {1, 3, 2},
                {2, 1, 3}, {2, 3, 1}, {3, 1, 2}, {3, 2, 1}});
        Assert.assertNull(perm.nextPermutation());
    }

    @Test
    public void testThreeElementPermutationUnsortedInput() {
        Permutations<Integer> perm = new Permutations<>(new Integer[]{2, 3, 1});
        List<Integer[]> allPermutations = allPermutations(perm, 3);
        assertEquals(allPermutations, new Integer[][]{{1, 2, 3}, {1, 3, 2},
                {2, 1, 3}, {2, 3, 1}, {3, 1, 2}, {3, 2, 1}});
        Assert.assertNull(perm.nextPermutation());
    }

    private List<Integer[]> allPermutations(Permutations<Integer> permutations,
                                            int numElements) {
        List<Integer[]> list = new ArrayList<>();
        Integer[] next = null;
        while ((next = permutations.nextPermutation()) != null) {
            list.add(Arrays.copyOf(next, next.length));
        }

        long expectedSize = ArithmeticUtils.factorial(numElements);
        Assert.assertEquals(expectedSize, list.size());
        return list;
    }

    private void assertEquals(List<Integer[]> actual, Integer[]... expected) {
        Assert.assertEquals(expected.length, actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Integer[] actualPerm = actual.get(i);
            Integer[] expectedPerm = expected[i];
            Assert.assertArrayEquals(expectedPerm, actualPerm);
        }
    }

}
