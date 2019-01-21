package de.jungblut.datastructure;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ListUtilsTest {

    private void assertMergedCorrectly(List<Integer> left, List<Integer> right) {
        // correct merge corresponds to the union of left and right and sorting
        List<Integer> expected = new ArrayList<>(left);
        expected.addAll(right);
        Collections.sort(expected);

        List<Integer> actual = ListUtils.merge(left, right);
        assertThat(actual, is(expected));
    }

    @Test
    public void testMergeHappyPath() {
        assertMergedCorrectly(asList(2, 3, 6, 11), asList(1, 2, 5, 7, 9, 10));
    }

    @Test
    public void testMergeHappyPathDuplicates() {
        assertMergedCorrectly(asList(2, 2, 3, 3, 6, 6, 11), asList(1, 1, 2, 2, 5, 5, 7, 7, 9, 10));
    }

    @Test
    public void testMergeHappyPathDuplicatesSameElement() {
        assertMergedCorrectly(asList(2, 2, 2, 2, 2, 2, 2), asList(2, 2, 2, 2, 2));
    }

    @Test
    public void testMergeInterleaved() {
        assertMergedCorrectly(asList(1, 3, 5, 7), asList(0, 2, 4, 6, 8));
    }

    @Test
    public void testMergeNegatives() {
        assertMergedCorrectly(asList(-10, -5, -1, 10), asList(-8, -3, 0, 5));
    }

    @Test
    public void testMergeLeftLonger() {
        assertMergedCorrectly(asList(0, 3, 11, 52, 93), asList(1, 2, 5, 8));
    }

    @Test
    public void testMergeRightLonger() {
        assertMergedCorrectly(asList(1, 2, 5, 8), asList(0, 3, 11, 52, 93));
    }

    @Test
    public void testMergeSingleElements() {
        assertMergedCorrectly(singletonList(1), singletonList(0));
    }

    @Test
    public void testMergeSingleElementsReversed() {
        assertMergedCorrectly(singletonList(0), singletonList(1));
    }

    @Test
    public void testMergeLeftEmpty() {
        assertMergedCorrectly(emptyList(), asList(1, 2, 5, 7, 9, 10));
    }

    @Test
    public void testMergeRightEmpty() {
        assertMergedCorrectly(asList(2, 3, 6, 11), emptyList());
    }

    @Test
    public void testMergeBothEmpty() {
        assertMergedCorrectly(Collections.<Integer>emptyList(), Collections.<Integer>emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeConsistency() {
        ListUtils.merge(Arrays.asList(5, 3, 2, 1), Arrays.asList(1, 2, 3, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeConsistencySingleSwapped() {
        ListUtils.merge(Arrays.asList(1, 2, 5, 4), Arrays.asList(1, 2, 3, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeConsistencySingleSwappedHungOverRight() {
        ListUtils.merge(Arrays.asList(1, 2, 4, 5), Arrays.asList(1, 2, 5, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeConsistencySingleSwappedHungOverLeft() {
        ListUtils.merge(Arrays.asList(1, 2, 5, 4), Arrays.asList(1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeConsistencySingleSwappedReverse() {
        ListUtils.merge(Arrays.asList(1, 2, 4, 5), Arrays.asList(1, 3, 2, 4, 5));
    }

    @Test(expected = NullPointerException.class)
    public void testMergeNullLeft() {
        ListUtils.merge(null, Collections.<Integer>emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void testMergeNullRight() {
        ListUtils.merge(Collections.<Integer>emptyList(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testMergeNullBoth() {
        ListUtils.merge(null, null);
    }

    // merge sort tests below

    @Test
    public void testMergeSortHappyPath() {
        assertSorted(asList(5, 2, 3, 1, 200, 5102));
    }

    @Test
    public void testMergeSortOddAmount() {
        assertSorted(asList(13, 6, 19, 91, 52));
    }

    @Test
    public void testMergeSortEvenAmount() {
        assertSorted(asList(13, 6, 19, 91, 55, 36));
    }

    @Test
    public void testMergeSortInverse() {
        assertSorted(asList(5, 4, 3, 2, 1, 0));
    }

    @Test
    public void testMergeSortNegatives() {
        assertSorted(asList(-5, 4, -3, -2, 1, -1222));
    }

    @Test
    public void testMergeSortSorted() {
        assertSorted(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    private void assertSorted(List<Integer> input) {
        List<Integer> copy = new ArrayList<>(input);
        List<Integer> mergeSorted = ListUtils.mergeSort(new ArrayList<>(input));
        Collections.sort(copy);
        assertThat(mergeSorted, is(copy));
    }
}
