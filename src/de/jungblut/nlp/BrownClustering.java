package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;

/**
 * Implementation of the brown clustering algorithm. This is work in progress,
 * as the hierarchy must be created to generate bit strings. Also
 * performance-wise there are several improvements to make it more faster.
 * 
 * @author thomas.jungblut
 * 
 */
public final class BrownClustering {

  /**
   * Clusters the given tokens of the documents into m clusters.
   * 
   * @param documents the documents that have n tokens.
   * @param m the number of clusters to return.
   * @return a list of m sets, containing related terms.
   */
  public static List<Set<String>> cluster(List<String[]> documents, int m) {

    HashMultiset<String> tokenCount = countToken(documents);
    String[] dictionary = createDictionary(tokenCount);

    SparseDoubleColumnMatrix transitionMatrix = computeTransitionMatrix(
        documents, dictionary);
    int transitionCount = (int) transitionMatrix.sum();
    // now do the real clustering
    ArrayList<Entry<String>> mostFrequentItems = VectorizerUtils
        .getMostFrequentItems(tokenCount);

    List<Set<String>> clusterAssignments = new ArrayList<>();

    for (int i = 0; i < Math.min(dictionary.length, m); i++) {
      addCluster(mostFrequentItems.get(i).getElement(), clusterAssignments);
    }
    for (int i = m; i < dictionary.length; i++) {
      // add a new item
      addCluster(mostFrequentItems.get(i).getElement(), clusterAssignments);
      // merge two items that have maximum amount of quality afterwards
      merge(clusterAssignments, transitionMatrix, tokenCount, dictionary,
          transitionCount);
    }

    // TODO create a hierarchy by carrying out m-1 merges
    return clusterAssignments;
  }

  private static void merge(List<Set<String>> clusterAssignments,
      SparseDoubleColumnMatrix transitionMatrix,
      HashMultiset<String> tokenCount, String[] dictionary,
      final int transitionCount) {
    // TODO these structures can be cached internally to speed things up
    final int size = clusterAssignments.size();
    double[] maxQuality = new double[size];
    int[] maxQualityIndex = new int[size];

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (i != j) {
          double quality = calculateQuality(clusterAssignments.get(i),
              clusterAssignments.get(j), transitionMatrix, tokenCount,
              dictionary, clusterAssignments.size(), transitionCount);
          if (quality > maxQuality[i]) {
            maxQuality[i] = quality;
            maxQualityIndex[i] = j;
          }
        }
      }
    }
    // merge maximum
    int maxIndex = ArrayUtils.maxIndex(maxQuality);
    int otherIndex = maxQualityIndex[maxIndex];

    Set<String> left = clusterAssignments.get(maxIndex);
    Set<String> right = clusterAssignments.get(otherIndex);
    Set<String> union = new HashSet<>(left.size() + right.size());
    union.addAll(left);
    union.addAll(right);
    // put it again into the list
    clusterAssignments.set(maxIndex, union);
    clusterAssignments.remove(otherIndex);
  }

  private static double calculateQuality(Set<String> set, Set<String> otherSet,
      SparseDoubleColumnMatrix transitionMatrix,
      HashMultiset<String> tokenCount, String[] dict, final int clusterCount,
      final int transitionCount) {

    int[] otherIndices = new int[otherSet.size()];
    int in = 0;
    double countOtherSet = 0d;
    for (String sx : otherSet) {
      otherIndices[in++] = Arrays.binarySearch(dict, sx);
      countOtherSet += tokenCount.count(sx);
    }
    double countSet = 0d;
    double transitionSum = 0d;
    for (String s : set) {
      int index = Arrays.binarySearch(dict, s);
      for (int otherIndex : otherIndices) {
        transitionSum += transitionMatrix.get(index, otherIndex);
      }
      countSet += tokenCount.count(s);
    }
    countSet = countSet / clusterCount;
    countOtherSet = countOtherSet / clusterCount;
    transitionSum = transitionSum / transitionCount;
    if (transitionSum == 0d) {
      // if we have no transition, we want to prevent NaNs when taking the log,
      // so we return the smallest number possible
      return Double.MIN_VALUE;
    }
    // note that we do not care about the constant entropy of the corpus
    return transitionSum
        * FastMath.log(transitionSum / (countSet * countOtherSet));
  }

  private static void addCluster(String item,
      List<Set<String>> clusterAssignments) {
    HashSet<String> set = new HashSet<>();
    set.add(item);
    clusterAssignments.add(set);
  }

  private static SparseDoubleColumnMatrix computeTransitionMatrix(
      List<String[]> documents, String[] dictionary) {
    SparseDoubleColumnMatrix transitionMatrix = new SparseDoubleColumnMatrix(
        dictionary.length, dictionary.length);
    for (String[] doc : documents) {
      for (int i = 0; i < doc.length - 1; i++) {
        int left = Arrays.binarySearch(dictionary, doc[i]);
        int right = Arrays.binarySearch(dictionary, doc[i + 1]);
        transitionMatrix.set(left, right,
            transitionMatrix.get(left, right) + 1d);
      }
    }
    return transitionMatrix;
  }

  private static String[] createDictionary(HashMultiset<String> tokenCount) {
    Set<String> elementSet = tokenCount.elementSet();
    String[] dictionary = elementSet.toArray(new String[elementSet.size()]);
    Arrays.sort(dictionary);
    return dictionary;
  }

  private static HashMultiset<String> countToken(List<String[]> documents) {
    HashMultiset<String> tokenCount = HashMultiset.create();
    for (String[] doc : documents) {
      for (int i = 0; i < doc.length; i++) {
        tokenCount.add(doc[i]);
      }
    }
    return tokenCount;
  }

}
