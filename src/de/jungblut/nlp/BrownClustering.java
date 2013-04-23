package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.math.sparse.SparseDoubleColumnMatrix;

public final class BrownClustering {

  public void cluster(List<String[]> documents, int m) {

    HashMultiset<String> tokenCount = countToken(documents);
    String[] dictionary = createDictionary(tokenCount);
    SparseDoubleColumnMatrix transitionMatrix = computeTransitionMatrix(
        documents, dictionary);
    // now do the real clustering
    int id = 0;
    List<BrownClusterNode> clusters = new ArrayList<>();
    ArrayList<Entry<String>> mostFrequentItems = VectorizerUtils
        .getMostFrequentItems(tokenCount);
    for (int i = 0; i < m; i++) {
      clusters.add(new BrownClusterNode(id++, mostFrequentItems.get(i)
          .getElement()));
    }
    for (int i = m; i < dictionary.length; i++) {
      // add a new item
      clusters.add(new BrownClusterNode(id++, mostFrequentItems.get(i)
          .getElement()));
      // merge two items that have maximum amount of quality afterwards
      
      
    }

  }

  private SparseDoubleColumnMatrix computeTransitionMatrix(
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

  private String[] createDictionary(HashMultiset<String> tokenCount) {
    Set<String> elementSet = tokenCount.elementSet();
    String[] dictionary = elementSet.toArray(new String[elementSet.size()]);
    elementSet = null;
    Arrays.sort(dictionary);
    return dictionary;
  }

  private HashMultiset<String> countToken(List<String[]> documents) {
    HashMultiset<String> tokenCount = HashMultiset.create();
    for (String[] doc : documents) {
      for (int i = 0; i < doc.length; i++) {
        tokenCount.add(doc[i]);
      }
    }
    return tokenCount;
  }

  public static class BrownClusterNode {

    private BrownClusterNode parent;

    private BrownClusterNode left;
    private BrownClusterNode right;

    private String value;
    private int clusterId;
    private double quality;

    public BrownClusterNode(int id, String value) {
      this.clusterId = id;
      this.value = value;
    }

  }

}
