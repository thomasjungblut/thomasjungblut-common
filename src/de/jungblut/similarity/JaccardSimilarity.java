package de.jungblut.similarity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class JaccardSimilarity {

	public static Set<String> tokenize(String key, int size) {
		Set<String> set = new LinkedHashSet<String>();
		for (int i = 0; i < key.length() - size + 1; i++) {
			int upperBound = i + size;
			set.add(key.substring(i, upperBound));
		}
		System.out.println(set);
		return set;
	}

	public static double distance(Set<String> set1, Set<String> set2) {
		double size = set1.size() + set2.size();
		if (size == 0)
			return 0;
		int oldSizeSet1 = set1.size();
		set1.retainAll(set2);
		// invert because this calculated the distance
		return (1 - (oldSizeSet1 - set1.size()) / size);
	}

	public static void main(String[] args) {
		Set<String> set1 = new HashSet<String>(Arrays.asList("abc", "def",
				"xyz"));
		Set<String> set2 = new HashSet<String>(Arrays.asList("xyz", "def",
				"abc"));
		System.out.println(distance(set1, set2));
		tokenize("Canon eos 500d", 3);
	}

}
