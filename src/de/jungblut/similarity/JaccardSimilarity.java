package de.jungblut.similarity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JaccardSimilarity implements Similarity {

	@Override
	public double measureDistance(Set<String> set1, Set<String> set2) {
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
		System.out.println(new JaccardSimilarity().measureDistance(set1, set2));
	}

}
