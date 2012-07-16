package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;
import de.jungblut.math.tuple.Tuple;

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
      Collections.addAll(tokenBagSet, arr);
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
    return wordFrequencyVectorize(setList, prepareWordCountToken(setList));
  }

  /**
   * Vectorizes a list of documents with the given wordcounts and tokens.
   */
  public static List<DoubleVector> wordFrequencyVectorize(
      List<String[]> setList, Tuple<HashMultiset<String>[], String[]> wordCounts) {

    HashMultiset<String>[] multiSets = wordCounts.getFirst();
    String[] tokenBagArray = wordCounts.getSecond();

    List<DoubleVector> vectorList = new ArrayList<>(setList.size());
    int i = 0;
    for (String[] arr : setList) {
      DoubleVector vector = new SparseDoubleVector(tokenBagArray.length);
      HashMultiset<String> hashMultiset = multiSets[i];
      for (String s : arr) {
        int foundIndex = Arrays.binarySearch(tokenBagArray, s);
        // simply ignore tokens we don't know
        if (foundIndex >= 0) {
          int count = hashMultiset.count(s);
          vector.set(foundIndex, count);
        }
      }
      vectorList.add(vector);
      i++;
    }

    return vectorList;
  }

  /**
   * This method is updating the wordcounts with the token bags for the given
   * documents.
   */
  public static Tuple<HashMultiset<String>[], String[]> updateWordFrequencyCounts(
      List<String[]> setList, String[] tokenBag) {

    HashSet<String> tokenBagSet = new HashSet<>(Arrays.asList(tokenBag));
    @SuppressWarnings("unchecked")
    HashMultiset<String>[] multiSets = new HashMultiset[setList.size()];
    int i = 0;
    for (String[] arr : setList) {
      multiSets[i] = HashMultiset.create();
      for (String anArr : arr) {
        if (tokenBagSet.contains(anArr)) {
          multiSets[i].add(anArr);
        }
      }
      i++;
    }

    return new Tuple<>(multiSets, tokenBag);
  }

  public static List<DoubleVector> wordFrequencyVectorize(String[]... vars) {
    return wordFrequencyVectorize(Arrays.asList(vars));
  }

  public static List<DoubleVector> tfIdfVectorize(String[]... vars) {
    return tfIdfVectorize(Arrays.asList(vars));
  }

  /**
   * Calculates the tf-idf of given documents with the given prepared
   * wordcounts.
   */
  public static List<DoubleVector> tfIdfVectorize(List<String[]> setList,
      Tuple<HashMultiset<String>[], String[]> prepareWordCountToken) {
    HashMultiset<String>[] multiSets = prepareWordCountToken.getFirst();
    String[] tokenBagArray = prepareWordCountToken.getSecond();

    List<DoubleVector> vectorList = new ArrayList<>(setList.size());
    int i = 0;
    for (String[] arr : setList) {
      DoubleVector vector = new SparseDoubleVector(tokenBagArray.length);
      HashMultiset<String> hashMultiset = multiSets[i];
      for (String s : arr) {
        int foundIndex = Arrays.binarySearch(tokenBagArray, s);
        if (foundIndex >= 0) {
          int wordCount = hashMultiset.count(s);
          double tfIdf = Math.log((double) arr.length / wordCount + 1.0d);
          vector.set(foundIndex, tfIdf);
        }
      }
      vectorList.add(vector);
      i++;
    }

    return vectorList;
  }

  /**
   * Given a multiset of generic elements we are going to return a list of all
   * the elements, sorted descending by their frequency.
   * 
   * @param set the given multiset.
   * @return a descending sorted list by frequency.
   */
  public static <E> ArrayList<Entry<E>> getMostFrequentItems(Multiset<E> set) {
    return getMostFrequentItems(set, null);
  }

  /**
   * Given a multiset of generic elements we are going to return a list of all
   * the elements, sorted descending by their frequency. Also can apply a filter
   * on the multiset, for example a filter for wordfrequency > 1.
   * 
   * @param set the given multiset.
   * @param filter if not null it filters by the given {@link Predicate}.
   * @return a descending sorted list by frequency.
   */
  public static <E> ArrayList<Entry<E>> getMostFrequentItems(Multiset<E> set,
      Predicate<Entry<E>> filter) {

    ArrayList<Entry<E>> list = Lists.newArrayList(filter == null ? set
        .entrySet() : Iterables.filter(set.entrySet(), filter));
    Collections.sort(list, new Comparator<Entry<E>>() {
      @Override
      public int compare(Entry<E> o1, Entry<E> o2) {
        return Integer.compare(o2.getCount(), o1.getCount());
      }
    });

    return list;
  }

  /**
   * Calculates the tf-idf of the given documents.
   */
  public static List<DoubleVector> tfIdfVectorize(List<String[]> setList) {
    Tuple<HashMultiset<String>[], String[]> prepareWordCountToken = prepareWordCountToken(setList);
    return tfIdfVectorize(setList, prepareWordCountToken);
  }

}
