package de.jungblut.datastructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeLists {

	public static List<Integer> merge(List<Integer> list1, List<Integer> list2) {
		List<Integer> newList = new ArrayList<Integer>(list1.size()
				+ list2.size());

		int offset1 = 0;
		int offset2 = 0;

		while (!list1.isEmpty() && !list2.isEmpty()) {
			Integer item1 = list1.get(offset1);
			Integer item2 = list2.get(offset2);
			if (item1 <= item2) {
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

	public static List<Integer> mergeSort(List<Integer> list) {
		if (list.size() <= 1) {
			return list;
		} else {
			int half = (int) (list.size() / 2);
			List<Integer> left = mergeSort(subList(list, 0, half - 1));
			List<Integer> right = mergeSort(subList(list, half, list.size() - 1));
			return merge(left, right);
		}
	}

	private static List<Integer> subList(List<Integer> list, int begin, int end) {
		List<Integer> temp = new ArrayList<Integer>(end - begin);
		for (int i = begin; i <= end; i++) {
			temp.add(list.get(i));
		}
		return temp;
	}

	public static void main(String[] args) {
		List<Integer> list1 = new ArrayList<Integer>(4);
		List<Integer> list2 = new ArrayList<Integer>(6);
		list2.addAll(Arrays.asList(1, 2, 5, 7, 9, 10));
		list1.addAll(Arrays.asList(2, 3, 6, 11));
		System.out.println(merge(list1, list2));

		System.out.println((mergeSort(Arrays.asList(100, 12, 136, 15, 26, 723,
				62, 6184, 8, 1, 3, 2, 72))));

	}
}
