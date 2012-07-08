package de.jungblut.datastructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

public class ListUtilsTest extends TestCase {

  List<Integer> list1 = new ArrayList<>();
  List<Integer> list2 = new ArrayList<>();

  List<Integer> unsortedList = Arrays.asList(100, 12, 136, 15, 26, 723, 62,
      6184, 8, 1, 3, 2, 72);

  List<Integer> mergedList = Arrays.asList(1, 2, 2, 3, 5, 6, 7, 9, 10, 11);

  {
    list2.addAll(Arrays.asList(1, 2, 5, 7, 9, 10));
    list1.addAll(Arrays.asList(2, 3, 6, 11));
  }

  @Test
  public void testMerge() {
    List<Integer> merge = ListUtils.merge(list1, list2);

    for (int i = 0; i < merge.size(); i++) {
      assertEquals(mergedList.get(i), merge.get(i));
    }

  }

  @Test
  public void TestMergeSort() {
    List<Integer> mergeSort = ListUtils.mergeSort(unsortedList);
    Collections.sort(unsortedList);

    for (int i = 0; i < unsortedList.size(); i++) {
      assertEquals(unsortedList.get(i), mergeSort.get(i));
    }
  }
}
