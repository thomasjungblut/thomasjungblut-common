package de.jungblut.datastructure;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * List util class for some fancy operations on generic lists.
 *
 * @author thomas.jungblut
 */
public final class ListUtils {

    private ListUtils() {
        throw new IllegalAccessError();
    }

    /**
     * Merges two sorted segments into a single sorted list.
     */
    public static <K extends Comparable<K>> List<K> merge(List<K> left,
                                                          List<K> right) {
        checkNotNull(left, "left");
        checkNotNull(left, "right");

        List<K> newList = new ArrayList<>(left.size() + right.size());

        int leftIndex = 0;
        int rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            K leftElement = left.get(leftIndex);
            K rightElement = right.get(rightIndex);

            K nextElement;
            if (leftElement.compareTo(rightElement) <= 0) {
                nextElement = leftElement;
                leftIndex++;
            } else {
                nextElement = rightElement;
                rightIndex++;
            }

            // check whether the lists are really sorted / consistent
            checkConsistency(newList, nextElement);
            newList.add(nextElement);
        }

        fillRemainder(left, newList, leftIndex);
        fillRemainder(right, newList, rightIndex);

        return newList;
    }

    private static <K extends Comparable<K>> void fillRemainder(List<K> oldList, List<K> newList, int currentIndex) {
        while (currentIndex < oldList.size()) {
            K nextElement = oldList.get(currentIndex++);
            checkConsistency(newList, nextElement);
            newList.add(nextElement);
        }
    }

    private static <K extends Comparable<K>> void checkConsistency(List<K> newList, K nextElement) {
        if (!newList.isEmpty()) {
            K lastElement = newList.get(newList.size() - 1);
            checkArgument(nextElement.compareTo(lastElement) >= 0,
                    "lists are not sorted, last element is not <= the current element");
        }
    }

    /**
     * MergeSorts the given list.
     */
    public static <K extends Comparable<K>> List<K> mergeSort(List<K> list) {
        if (list.size() <= 1) {
            return list;
        } else {
            int half = list.size() / 2;
            List<K> left = mergeSort(list.subList(0, half));
            List<K> right = mergeSort(list.subList(half, list.size()));
            return merge(left, right);
        }
    }

}
