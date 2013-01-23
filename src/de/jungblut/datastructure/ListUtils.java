package de.jungblut.datastructure;

import java.util.ArrayList;
import java.util.List;

/**
 * List util class for some fancy operations on generic lists.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class ListUtils {

  /**
   * Merges two sorted segments into a single sorted list.
   */
  public static <K extends Comparable<K>> List<K> merge(List<K> list1,
      List<K> list2) {
    List<K> newList = new ArrayList<>(list1.size() + list2.size());

    int offset1 = 0;
    int offset2 = 0;

    while (!list1.isEmpty() && !list2.isEmpty()) {
      K item1 = list1.get(offset1);
      K item2 = list2.get(offset2);
      if (item1.compareTo(item2) <= 0) {
        newList.add(item1);
        list1.remove(offset1);
      } else {
        newList.add(item2);
        list2.remove(offset2);
      }

      if (offset1 == list1.size()) {
        newList.addAll(list2);
      }
      if (offset2 == list2.size()) {
        newList.addAll(list1);
      }
    }

    return newList;
  }

  /**
   * MergeSorts the given list.
   */
  public static <K extends Comparable<K>> List<K> mergeSort(List<K> list) {
    if (list.size() <= 1) {
      return list;
    } else {
      int half = list.size() / 2;
      List<K> left = mergeSort(subList(list, 0, half - 1));
      List<K> right = mergeSort(subList(list, half, list.size() - 1));
      return merge(left, right);
    }
  }

  /**
   * Sublists the given list.
   */
  public static <K> List<K> subList(List<K> list, int begin, int end) {
    List<K> temp = new ArrayList<>(end - begin);
    for (int i = begin; i <= end; i++) {
      temp.add(list.get(i));
    }
    return temp;
  }
}
