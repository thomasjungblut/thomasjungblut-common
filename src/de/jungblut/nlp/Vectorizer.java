package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.HashMultiset;

public class Vectorizer {

  /**
   * This uses dense vectors, so not very appropriate for larger text with many
   * words, uses huge amounts of memory. TODO replace when I have a more
   * efficient implementation of sparse vectors
   */
  public static List<double[]> wordFrequencyVectorize(List<String[]> setList) {
    HashSet<String> tokenBagSet = new HashSet<>();
    @SuppressWarnings("unchecked")
    HashMultiset<String>[] multiSets = new HashMultiset[setList.size()];
    int i = 0;
    for (String[] arr : setList) {
      for (String s : arr) {
        tokenBagSet.add(s);
      }
      multiSets[i] = HashMultiset.create();
      multiSets[i].addAll(Arrays.asList(arr));
      i++;
    }

    String[] tokenBagArray = tokenBagSet
        .toArray(new String[tokenBagSet.size()]);
    Arrays.sort(tokenBagArray);

    List<double[]> vectorList = new ArrayList<double[]>(setList.size());

    i = 0;
    for (String[] arr : setList) {
      double[] vector = new double[tokenBagSet.size()];
      HashMultiset<String> hashMultiset = multiSets[i];
      for (String s : arr) {
        int foundIndex = Arrays.binarySearch(tokenBagArray, s);
        vector[foundIndex] = hashMultiset.count(s);
      }
      vectorList.add(vector);
      i++;
    }

    return vectorList;
  }

  public static List<double[]> wordFrequencyVectorize(String[]... vars) {
    return wordFrequencyVectorize(Arrays.asList(vars));
  }

}
