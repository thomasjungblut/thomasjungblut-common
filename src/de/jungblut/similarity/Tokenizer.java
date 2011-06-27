package de.jungblut.similarity;

import java.util.LinkedHashSet;
import java.util.Set;

public class Tokenizer {

	public static Set<String> tokenize(String key, int size) {
		Set<String> set = new LinkedHashSet<String>();
		if (key.length() < size) {
			set.add(key);
			return set;
		}
		for (int i = 0; i < key.length() - size + 1; i++) {
			int upperBound = i + size;
			set.add(key.substring(i, upperBound));
		}
		return set;
	}

}
