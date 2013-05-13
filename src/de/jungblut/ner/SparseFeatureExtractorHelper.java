package de.jungblut.ner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Convenient helper for creating vectors out of text features for sequence
 * learning. Inspired by Coursera's NLP Class PA4.
 * 
 * @author thomas.jungblut
 * 
 */
public final class SparseFeatureExtractorHelper<K> {

  private final List<K> words;
  private final List<Integer> labels;
  private final SequenceFeatureExtractor<K> extractor;
  private final HashSet<Integer> classSet;

  private int classes;
  private String[] dicts;

  /**
   * Constructs this feature factory.
   * 
   * @param words a list of words in sequence to learn on.
   * @param labels the corresponding labels in parallel to the words.
   * @param extractor the core implementation of the feature extractor.
   */
  public SparseFeatureExtractorHelper(List<K> words, List<Integer> labels,
      SequenceFeatureExtractor<K> extractor) {
    this.words = words;
    this.labels = labels;
    this.extractor = extractor;
    // calculate how many different classes are there (assuming they are
    // starting with 0)
    this.classSet = new HashSet<>(labels);
    this.classes = classSet.size();
  }

  /**
   * Constructs this feature factory via a given dictionary.
   * 
   * @param words a list of words in sequence to learn on.
   * @param labels the corresponding labels in parallel to the words.
   * @param extractor the core implementation of the feature extractor.
   * @param dictionary an already given dictionary.
   */
  public SparseFeatureExtractorHelper(List<K> words, List<Integer> labels,
      SequenceFeatureExtractor<K> extractor, String[] dictionary) {
    this(words, labels, extractor);
    this.dicts = dictionary;
  }

  /**
   * Vectorizes the given data from the constructor. Internally builds a
   * dictionary that can be saved to vectorize additional data with
   * {@link #vectorizeAdditionals(List, List)}.
   * 
   * @return a {@link Tuple} with the features in the first dimension, and on
   *         the second the outcome.
   */
  public Tuple<DoubleVector[], DenseDoubleVector[]> vectorize() {
    return extractInternal(words, labels);
  }

  /**
   * Vectorizes the given word.
   * 
   * @return the feature for the given word.
   */
  public DoubleVector vectorize(K word) {
    return vectorize(word, null);
  }

  /**
   * Vectorizes the given word with the previous outcome.
   * 
   * @return the feature for the given word.
   */
  public DoubleVector vectorize(K word, Integer lastLabel) {
    List<String> computedFeatures = extractor.computeFeatures(
        Arrays.asList(word), lastLabel == null ? 0 : lastLabel, 0);
    DoubleVector feature = new SparseDoubleVector(dicts.length);
    for (String feat : computedFeatures) {
      int index = Arrays.binarySearch(dicts, feat);
      if (index >= 0) {
        feature.set(index, 1d);
      }
    }
    return feature;
  }

  /**
   * Vectorizes the given data. Internally uses a dictionary that was created by
   * {@link #vectorize()} or creates one on this data.
   * 
   * @return a {@link Tuple} with the features in the first dimension, and on
   *         the second the outcome.
   */
  public Tuple<DoubleVector[], DenseDoubleVector[]> vectorizeAdditionals(
      List<K> words, List<Integer> labels) {
    return extractInternal(words, labels);
  }

  /**
   * Vectorizes the given data for each label. Internally uses a dictionary that
   * was created by {@link #vectorize()} or creates one on this data.
   */
  public DoubleVector[] vectorizeEachLabel(List<K> words) {
    List<List<String>> stringFeatures = new ArrayList<>();
    for (int i = 0; i < words.size(); i++) {
      if (i == 0) {
        stringFeatures.add(extractor.computeFeatures(words, 0, i));
      } else {
        for (int prevLabel : classSet) {
          stringFeatures.add(extractor.computeFeatures(words, prevLabel, i));
        }
      }
    }

    DoubleVector[] features = new DoubleVector[stringFeatures.size()];
    final int dimension = dicts.length;
    // translate the feature vector
    for (int i = 0; i < features.length; i++) {
      features[i] = new SparseDoubleVector(dimension);
      for (String feat : stringFeatures.get(i)) {
        int index = Arrays.binarySearch(dicts, feat);
        if (index >= 0) {
          features[i].set(index, 1d);
        }
      }
    }
    return features;
  }

  /**
   * @return the built dictionary.
   */
  public String[] getDictionary() {
    return this.dicts;
  }

  /**
   * Generates the feature vectors and the dictionary.
   */
  private Tuple<DoubleVector[], DenseDoubleVector[]> extractInternal(
      List<K> words, List<Integer> labels) {
    List<List<String>> stringFeatures = new ArrayList<>();
    for (int i = 0; i < words.size(); i++) {
      stringFeatures.add(extractor.computeFeatures(words,
          i == 0 ? 0 : labels.get(i - 1), i));
    }

    DoubleVector[] features = new DoubleVector[stringFeatures.size()];
    DenseDoubleVector[] outcome = new DenseDoubleVector[stringFeatures.size()];
    // skip if we already have a dictionary
    if (dicts == null) {
      // now build the feature space out of the strings, sort the features, so
      // they translate to an index in an array.
      HashSet<String> set = new HashSet<>();
      for (List<String> feat : stringFeatures) {
        set.addAll(feat);
      }
      // sort it for binary search
      dicts = set.toArray(new String[set.size()]);
      Arrays.sort(dicts);
    }
    final int dimension = dicts.length;
    // translate the feature vector
    for (int i = 0; i < features.length; i++) {
      features[i] = new SparseDoubleVector(dimension);
      for (String feat : stringFeatures.get(i)) {
        int index = Arrays.binarySearch(dicts, feat);
        if (index >= 0) {
          features[i].set(index, 1d);
        }
      }
      outcome[i] = new DenseDoubleVector(classes == 2 ? 1 : classes);
      if (classes == 2) {
        outcome[i].set(0, labels.get(i));
      } else {
        outcome[i].set(labels.get(i), 1d);
      }
    }

    return new Tuple<>(features, outcome);
  }

}
