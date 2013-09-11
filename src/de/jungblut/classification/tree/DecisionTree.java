package de.jungblut.classification.tree;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * A decision tree that can be used for classification with numerical or
 * categorical features. The tree is built by maximizing information gain using
 * the id3 algorithm.
 * 
 * @author thomasjungblut
 * 
 */
public final class DecisionTree extends AbstractClassifier {

  private static final double LOG2 = FastMath.log(2);

  private AbstractTreeNode rootNode;
  private FeatureType[] featureTypes;

  // do we only have two outcomes?
  private boolean binaryClassification = true;
  private int outcomeDimension;
  private int numFeatures;

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    Preconditions.checkArgument(features.length == outcome.length,
        "Number of examples and outcomes must match!");
    // assume all nominal
    if (featureTypes == null) {
      featureTypes = new FeatureType[features[0].getDimension()];
      Arrays.fill(featureTypes, FeatureType.NOMINAL);
    }
    Preconditions.checkArgument(
        featureTypes.length == features[0].getDimension(),
        "FeatureType length must match the dimension of the features!");
    binaryClassification = outcome[0].getDimension() == 1;
    if (binaryClassification) {
      outcomeDimension = 2;
    } else {
      outcomeDimension = outcome[0].getDimension();
    }
    numFeatures = features[0].getDimension();

    // all features are possible here
    TIntHashSet possibleFeatureIndices = new TIntHashSet();
    for (int i = 0; i < features[0].getDimension(); i++) {
      possibleFeatureIndices.add(i);
    }
    // recursively build the tree:
    // note that we use linked lists to remove examples we don't need, linked
    // structures do not require costly copy operations after removal
    rootNode = build(Lists.newLinkedList(Arrays.asList(features)),
        Lists.newLinkedList(Arrays.asList(outcome)), possibleFeatureIndices);
  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    int clz = rootNode.predict(features);
    if (clz == -1) {
      // let's assume the default case ("negative") here, instead of making NPEs
      // in other areas, as the callers aren't nullsafe
      clz = 0;
    }
    if (binaryClassification) {
      return new DenseDoubleVector(new double[] { clz });
    } else {
      DoubleVector vec = outcomeDimension > 10 ? new SparseDoubleVector(
          outcomeDimension) : new DenseDoubleVector(outcomeDimension);
      vec.set(clz, 1);
      return vec;
    }
  }

  public AbstractTreeNode build(List<DoubleVector> features,
      List<DoubleVector> outcome, TIntHashSet possibleFeatureIndices) {

    int[] countOutcomeClasses = getPossibleClasses(outcome);
    TIntHashSet notZeroClasses = new TIntHashSet();
    for (int i = 0; i < countOutcomeClasses.length; i++) {
      if (countOutcomeClasses[i] != 0) {
        notZeroClasses.add(i);
      }
    }

    // if we only have a single class to predict, we will create a leaf to
    // predict it
    if (notZeroClasses.size() == 1) {
      return new LeafNode(notZeroClasses.iterator().next());
    }

    // if we don't have anymore features to split on, we will use the majority
    // class and predict it
    if (possibleFeatureIndices.isEmpty()) {
      return new LeafNode(ArrayUtils.maxIndex(countOutcomeClasses));
    }

    // now we can evaluate the infogain for every possible feature and choose
    // that one that maximizes it and split on it
    double targetEntropy = getEntropy(countOutcomeClasses);
    double[] infoGain = new double[numFeatures];
    for (int featureIndex : possibleFeatureIndices.toArray()) {
      infoGain[featureIndex] = targetEntropy
          - calculateEntropyGivenFeature(featureIndex, countOutcomeClasses,
              features, outcome);
    }

    // pick the split with highest info gain
    int bestSplitIndex = ArrayUtils.maxIndex(infoGain);
    TIntHashSet uniqueFeatures = getNominalValues(bestSplitIndex, features);
    if (featureTypes[bestSplitIndex] == FeatureType.NOMINAL) {
      NominalNode node = new NominalNode();
      node.splitAttributeIndex = bestSplitIndex;
      node.children = new AbstractTreeNode[uniqueFeatures.size()];
      node.nominalSplitValues = new int[uniqueFeatures.size()];
      int cIndex = 0;
      for (int nominalValue : uniqueFeatures.toArray()) {
        node.nominalSplitValues[cIndex] = nominalValue;
        Tuple<List<DoubleVector>, List<DoubleVector>> filtered = filterNominal(
            features, outcome, bestSplitIndex, nominalValue);
        TIntHashSet newPossibleFeatures = new TIntHashSet(
            possibleFeatureIndices);
        newPossibleFeatures.remove(bestSplitIndex);
        node.children[cIndex] = build(filtered.getFirst(),
            filtered.getSecond(), newPossibleFeatures);
        cIndex++;
      }
      return node;
    }
    // TODO numerical features

    return null;
  }

  /**
   * Filters all examples where the feature at the given index has NOT the
   * specific value. So the returned lists contain only vectors where the
   * feature has the specific value.
   * 
   * @return a new tuple of two new lists (features and their outcome).
   */
  private Tuple<List<DoubleVector>, List<DoubleVector>> filterNominal(
      List<DoubleVector> features, List<DoubleVector> outcome,
      int bestSplitIndex, int nominalValue) {

    List<DoubleVector> newFeatures = Lists.newLinkedList();
    List<DoubleVector> newOutcomes = Lists.newLinkedList();

    Iterator<DoubleVector> featureIterator = features.iterator();
    Iterator<DoubleVector> outcomeIterator = outcome.iterator();
    while (featureIterator.hasNext()) {
      DoubleVector feature = featureIterator.next();
      DoubleVector out = outcomeIterator.next();
      if (((int) feature.get(bestSplitIndex)) == nominalValue) {
        newFeatures.add(feature);
        newOutcomes.add(out);
      }
    }

    return new Tuple<>(newFeatures, newOutcomes);
  }

  private double calculateEntropyGivenFeature(int featureIndex,
      int[] countOutcomeClasses, List<DoubleVector> features,
      List<DoubleVector> outcome) {

    if (featureTypes[featureIndex] == FeatureType.NOMINAL) {
      TIntObjectHashMap<int[]> featureValueOutcomeCount = new TIntObjectHashMap<>();
      TIntIntHashMap rowSums = new TIntIntHashMap();
      int numFeatures = 0;
      Iterator<DoubleVector> featureIterator = features.iterator();
      Iterator<DoubleVector> outcomeIterator = outcome.iterator();
      while (featureIterator.hasNext()) {
        DoubleVector feature = featureIterator.next();
        DoubleVector out = outcomeIterator.next();
        int classIndex = 0;
        if (binaryClassification) {
          classIndex = (int) out.get(0);
        } else {
          classIndex = out.maxIndex();
        }
        int nominalFeatureValue = (int) feature.get(featureIndex);
        int[] is = featureValueOutcomeCount.get(nominalFeatureValue);
        if (is == null) {
          is = new int[outcomeDimension];
          featureValueOutcomeCount.put(nominalFeatureValue, is);
        }
        is[classIndex]++;
        rowSums.put(nominalFeatureValue, rowSums.get(nominalFeatureValue) + 1);
        numFeatures++;
      }
      double entropySum = 0d;
      // now we can calculate the entropy
      TIntObjectIterator<int[]> iterator = featureValueOutcomeCount.iterator();
      while (iterator.hasNext()) {
        iterator.advance();
        int[] outcomeCounts = iterator.value();
        double condEntropy = rowSums.get(iterator.key()) / (double) numFeatures
            * getEntropy(outcomeCounts);
        entropySum += condEntropy;
      }
      return entropySum;
    }
    // TODO what needs to be done for numerical features?

    return 0d;
  }

  /**
   * @return a set of nominal values of that feature index given the examples
   *         that contain this feature.
   */
  private TIntHashSet getNominalValues(int featureIndex,
      List<DoubleVector> features) {
    TIntHashSet uniqueFeatures = new TIntHashSet();
    for (DoubleVector vec : features) {
      int featureValue = (int) vec.get(featureIndex);
      uniqueFeatures.add(featureValue);
    }
    return uniqueFeatures;
  }

  /**
   * @return an array from 0-outcome dimension that has a count on every feature
   *         index representing how often it occurred.
   */
  private int[] getPossibleClasses(List<DoubleVector> outcome) {
    int[] clzs = new int[outcomeDimension];
    for (DoubleVector out : outcome) {
      if (binaryClassification) {
        clzs[(int) out.get(0)]++;
      } else {
        clzs[out.maxIndex()]++;
      }
    }

    return clzs;
  }

  /**
   * @return the entropy of the given prediction class counts.
   */
  static double getEntropy(int[] outcomeCounter) {
    double entropySum = 0d;
    double sum = 0d;
    for (int x : outcomeCounter) {
      sum += x;
    }
    for (int x : outcomeCounter) {
      if (x == 0) {
        return 0d;
      }
      double conditionalProbability = x / sum;
      entropySum -= (conditionalProbability * log2(conditionalProbability));
    }

    return entropySum;
  }

  /**
   * @return the log2 of the given input.
   */
  private static double log2(double num) {
    return FastMath.log(num) / LOG2;
  }

}
