package de.jungblut.similarity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CosineSimilarity implements Similarity {

  @Override
  public double measureDistance(Set<String> set1, Set<String> set2) {
    final HashSet<String> allSet = new HashSet<>();
    final int termsInString1 = set1.size();
    final int termsInString2 = set2.size();

    allSet.addAll(set2);
    allSet.addAll(set1);
    final int commonTerms = (termsInString1 + termsInString2) - allSet.size();

    return (float) (commonTerms)
        / (float) (Math.pow((float) termsInString1, 0.5f) * Math.pow(
            (float) termsInString2, 0.5f));
  }

  public static void main(String[] args) {
    Set<String> set1 = new HashSet<>(Arrays.asList("abc", "def", "xyz"));
    Set<String> set2 = new HashSet<>(Arrays.asList("xyz", "def", "abc"));
    System.out.println(new CosineSimilarity().measureDistance(set1, set2));
  }

}
