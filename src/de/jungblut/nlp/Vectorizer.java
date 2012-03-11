package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.HashMultiset;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;
import de.jungblut.util.Tuple;

public final class Vectorizer {

  /**
   * Prepares a wordcount per document and a token bag of all documents.
   */
  public static Tuple<HashMultiset<String>[], String[]> prepareWordCountToken(
      List<String[]> setList) {
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
    return new Tuple<>(multiSets, tokenBagArray);
  }

  /**
   * In-Memory word count vectorizer
   */
  public static List<DoubleVector> wordFrequencyVectorize(List<String[]> setList) {
    List<DoubleVector> vectorList = new ArrayList<DoubleVector>(setList.size());

    Tuple<HashMultiset<String>[], String[]> prepareWordCountToken = prepareWordCountToken(setList);
    HashMultiset<String>[] multiSets = prepareWordCountToken.getFirst();
    String[] tokenBagArray = prepareWordCountToken.getSecond();

    int i = 0;
    for (String[] arr : setList) {
      DoubleVector vector = new SparseDoubleVector(tokenBagArray.length);
      HashMultiset<String> hashMultiset = multiSets[i];
      for (String s : arr) {
        int foundIndex = Arrays.binarySearch(tokenBagArray, s);
        vector.set(foundIndex, hashMultiset.count(s));
      }
      vectorList.add(vector);
      i++;
    }

    return vectorList;
  }

  public static List<DoubleVector> wordFrequencyVectorize(String[]... vars) {
    return wordFrequencyVectorize(Arrays.asList(vars));
  }

  public static List<DoubleVector> tfIdfVectorize(String[]... vars) {
    return tfIdfVectorize(Arrays.asList(vars));
  }

  /**
   * Calculates the tf-idf of the given documents.
   */
  public static List<DoubleVector> tfIdfVectorize(List<String[]> setList) {
    List<DoubleVector> vectorList = new ArrayList<DoubleVector>(setList.size());

    Tuple<HashMultiset<String>[], String[]> prepareWordCountToken = prepareWordCountToken(setList);
    HashMultiset<String>[] multiSets = prepareWordCountToken.getFirst();
    String[] tokenBagArray = prepareWordCountToken.getSecond();

    int i = 0;
    for (String[] arr : setList) {
      DoubleVector vector = new SparseDoubleVector(tokenBagArray.length);
      HashMultiset<String> hashMultiset = multiSets[i];
      for (String s : arr) {
        int foundIndex = Arrays.binarySearch(tokenBagArray, s);
        int wordCount = hashMultiset.count(s);
        double tfIdf = Math.log((double) arr.length / wordCount + 1.0d);
        vector.set(foundIndex, tfIdf);
      }
      vectorList.add(vector);
      i++;
    }

    return vectorList;
  }

}
