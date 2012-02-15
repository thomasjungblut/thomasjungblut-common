package de.jungblut.similarity;

import java.util.Set;

public interface Similarity {

    public double measureDistance(Set<String> set1, Set<String> set2);

}