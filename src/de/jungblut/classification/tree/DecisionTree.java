package de.jungblut.classification.tree;

import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TDoubleHashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

  private TreeNode rootNode;
  private FeatureType[] featureTypes;
  private int numRandomFeaturesToChoose;
  private long seed = System.currentTimeMillis();

  // default is binary classification 0 or 1.
  private boolean binaryClassification = true;
  private int outcomeDimension;
  private int numFeatures;

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    Preconditions.checkArgument(features.length == outcome.length,
        "Number of examples and outcomes must match!");
    // assume all nominal if nothing was set
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
    TIntHashSet possibleFeatureIndices = getPossibleFeatures();
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

  /**
   * Sets the type of feature per index. This should match the inputted number
   * of features in the training method. If this isn't set at all, all
   * attributes are assumed to be nominal.
   */
  public void setFeatureTypes(FeatureType[] featureTypes) {
    this.featureTypes = featureTypes;
  }

  /**
   * Sets the number of random features to choose from all features.Zero,
   * negative numbers or numbers greater than the really available features
   * indicate all features to be used.
   */
  public void setNumRandomFeaturesToChoose(int numRandomFeaturesToChoose) {
    this.numRandomFeaturesToChoose = numRandomFeaturesToChoose;
  }

  /**
   * Sets the seed for a random number generator if used.
   */
  public void setSeed(long seed) {
    this.seed = seed;
  }

  void setNumFeatures(int numFeatures) {
    this.numFeatures = numFeatures;
  }

  /**
   * @return the set of possible features
   */
  TIntHashSet getPossibleFeatures() {
    // all features are possible here
    TIntHashSet possibleFeatureIndices = new TIntHashSet();
    for (int i = 0; i < numFeatures; i++) {
      possibleFeatureIndices.add(i);
    }
    possibleFeatureIndices = chooseRandomFeatures(possibleFeatureIndices);
    return possibleFeatureIndices;
  }

  /**
   * @return a random subset of the features.
   */
  TIntHashSet chooseRandomFeatures(TIntHashSet possibleFeatureIndices) {
    if (numRandomFeaturesToChoose > 0
        && numRandomFeaturesToChoose < numFeatures) {
      // we now remove the difference from the set
      Random rnd = new Random(seed);
      while (possibleFeatureIndices.size() > numRandomFeaturesToChoose) {
        possibleFeatureIndices.remove(rnd.nextInt(numFeatures));
      }
    }
    return possibleFeatureIndices;
  }

  /**
   * Recursively build the decision tree in a top down fashion.
   */
  private TreeNode build(List<DoubleVector> features,
      List<DoubleVector> outcome, TIntHashSet possibleFeatureIndices) {

    // TODO should we select a subset of features at every tree level?

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
    Split[] infoGain = new Split[numFeatures];
    for (int featureIndex : possibleFeatureIndices.toArray()) {
      infoGain[featureIndex] = calculateInformationGain(targetEntropy,
          featureIndex, countOutcomeClasses, features, outcome);
    }

    // pick the split with highest info gain
    int maxIndex = 0;
    double maxGain = infoGain[maxIndex] != null ? infoGain[maxIndex]
        .getInformationGain() : Integer.MIN_VALUE;
    for (int i = 1; i < infoGain.length; i++) {
      if (infoGain[i] != null && infoGain[i].getInformationGain() > maxGain) {
        maxGain = infoGain[i].getInformationGain();
        maxIndex = i;
      }
    }
    Split bestSplit = infoGain[maxIndex];
    int bestSplitIndex = bestSplit.getSplitAttributeIndex();
    if (featureTypes[bestSplitIndex] == FeatureType.NOMINAL) {
      TIntHashSet uniqueFeatures = getNominalValues(bestSplitIndex, features);
      NominalNode node = new NominalNode();
      node.splitAttributeIndex = bestSplitIndex;
      node.children = new TreeNode[uniqueFeatures.size()];
      node.nominalSplitValues = new int[uniqueFeatures.size()];
      int cIndex = 0;
      for (int nominalValue : uniqueFeatures.toArray()) {
        node.nominalSplitValues[cIndex] = nominalValue;
        Tuple<List<DoubleVector>, List<DoubleVector>> filtered = filterNominal(
            features, outcome, bestSplitIndex, nominalValue);
        TIntHashSet newPossibleFeatures = new TIntHashSet(
            possibleFeatureIndices);
        // remove that feature
        newPossibleFeatures.remove(bestSplitIndex);
        node.children[cIndex] = build(filtered.getFirst(),
            filtered.getSecond(), newPossibleFeatures);
        cIndex++;
      }
      return node;
    } else if (featureTypes[bestSplitIndex] == FeatureType.NUMERICAL) {
      TIntHashSet newPossibleFeatures = new TIntHashSet(possibleFeatureIndices);
      Tuple<List<DoubleVector>, List<DoubleVector>> filterNumeric = filterNumericLower(
          features, outcome, bestSplitIndex, bestSplit.getNumericalSplitValue());
      Tuple<List<DoubleVector>, List<DoubleVector>> filterNumericHigher = filterNumericHigher(
          features, outcome, bestSplitIndex, bestSplit.getNumericalSplitValue());

      if (filterNumeric.getFirst().isEmpty()
          || filterNumericHigher.getFirst().isEmpty()) {
        newPossibleFeatures.remove(bestSplitIndex);
      } else {
        // we changed something, thus we can unselect all numerical features
        for (int i = 0; i < featureTypes.length; i++) {
          if (featureTypes[i] == FeatureType.NUMERICAL) {
            newPossibleFeatures.add(i);
          }
        }
        newPossibleFeatures = chooseRandomFeatures(newPossibleFeatures);
      }

      // build subtrees
      TreeNode lower = build(filterNumeric.getFirst(),
          filterNumeric.getSecond(), newPossibleFeatures);
      TreeNode higher = build(filterNumericHigher.getFirst(),
          filterNumericHigher.getSecond(), newPossibleFeatures);
      // now we can return this completed node
      return new NumericalNode(bestSplitIndex,
          bestSplit.getNumericalSplitValue(), lower, higher);
    }

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

  /**
   * @return the feature and outcome where the feature is strictly higher than
   *         the split value.
   */
  private Tuple<List<DoubleVector>, List<DoubleVector>> filterNumericHigher(
      List<DoubleVector> features, List<DoubleVector> outcome,
      int bestSplitIndex, double splitValue) {

    List<DoubleVector> newFeatures = Lists.newLinkedList();
    List<DoubleVector> newOutcomes = Lists.newLinkedList();

    Iterator<DoubleVector> featureIterator = features.iterator();
    Iterator<DoubleVector> outcomeIterator = outcome.iterator();
    while (featureIterator.hasNext()) {
      DoubleVector feature = featureIterator.next();
      DoubleVector out = outcomeIterator.next();
      if (feature.get(bestSplitIndex) > splitValue) {
        newFeatures.add(feature);
        newOutcomes.add(out);
      }
    }

    return new Tuple<>(newFeatures, newOutcomes);
  }

  /**
   * @return the feature and outcome where the feature is strictly lower than or
   *         equal to the split value.
   */
  private Tuple<List<DoubleVector>, List<DoubleVector>> filterNumericLower(
      List<DoubleVector> features, List<DoubleVector> outcome,
      int bestSplitIndex, double splitValue) {

    List<DoubleVector> newFeatures = Lists.newLinkedList();
    List<DoubleVector> newOutcomes = Lists.newLinkedList();

    Iterator<DoubleVector> featureIterator = features.iterator();
    Iterator<DoubleVector> outcomeIterator = outcome.iterator();
    while (featureIterator.hasNext()) {
      DoubleVector feature = featureIterator.next();
      DoubleVector out = outcomeIterator.next();
      if (feature.get(bestSplitIndex) <= splitValue) {
        newFeatures.add(feature);
        newOutcomes.add(out);
      }
    }

    return new Tuple<>(newFeatures, newOutcomes);
  }

  private Split calculateInformationGain(double overallEntropy,
      int featureIndex, int[] countOutcomeClasses, List<DoubleVector> features,
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
        int classIndex = getOutcomeClassIndex(out);
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
      return new Split(featureIndex, overallEntropy - entropySum);
    } else if (featureTypes[featureIndex] == FeatureType.NUMERICAL) {

      Iterator<DoubleVector> featureIterator = features.iterator();
      TDoubleHashSet possibleFeatureValues = new TDoubleHashSet();
      while (featureIterator.hasNext()) {
        DoubleVector feature = featureIterator.next();
        possibleFeatureValues.add(feature.get(featureIndex));
      }
      double bestInfogain = -1;
      double bestSplit = 0.0;
      TDoubleIterator iterator = possibleFeatureValues.iterator();
      while (iterator.hasNext()) {
        double value = iterator.next();
        double ig = computeNumericalInfogain(features, outcome, overallEntropy,
            1d / features.size(), featureIndex, value);
        if (ig > bestInfogain) {
          bestInfogain = ig;
          bestSplit = value;
        }
      }
      return new Split(featureIndex, bestInfogain, bestSplit);
    }

    return null;
  }

  // TODO this can be constant if the lists are sorted on the feature value
  private double computeNumericalInfogain(List<DoubleVector> features,
      List<DoubleVector> outcome, double overallEntropy, double invDatasize,
      int featureIndex, double value) {

    // 0 denotes lower than or equal, 1 denotes higher
    int[][] counts = new int[2][outcomeDimension];
    int lowCount = 0;
    int highCount = 0;
    Arrays.fill(counts, new int[outcomeDimension]);
    Iterator<DoubleVector> featureIterator = features.iterator();
    Iterator<DoubleVector> outcomeIterator = outcome.iterator();
    while (featureIterator.hasNext()) {
      DoubleVector feature = featureIterator.next();
      DoubleVector out = outcomeIterator.next();
      int idx = getOutcomeClassIndex(out);
      if (feature.get(featureIndex) > value) {
        counts[1][idx]++;
        highCount++;
      } else {
        counts[0][idx]++;
        lowCount++;
      }
    }

    // discount the lower set
    overallEntropy -= (lowCount * invDatasize * getEntropy(counts[0]));
    // and the higher one
    overallEntropy -= (highCount * invDatasize * getEntropy(counts[1]));
    return overallEntropy;
  }

  private int getOutcomeClassIndex(DoubleVector out) {
    int classIndex = 0;
    if (binaryClassification) {
      classIndex = (int) out.get(0);
    } else {
      classIndex = out.maxIndex();
    }
    return classIndex;
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
