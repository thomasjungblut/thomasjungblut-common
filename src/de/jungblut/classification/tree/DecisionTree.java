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
 * the ID3 algorithm. If no featureTypes were supplied, the default is assumed
 * to be nominal features at all feature dimensions. <br/>
 * Instances can be created by the static factory methods #create().
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

  // use the static factory methods!
  private DecisionTree() {
  }

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
   * @return a random subset of the features.
   */
  TIntHashSet chooseRandomFeatures(TIntHashSet possibleFeatureIndices) {
    // make a copy
    if (numRandomFeaturesToChoose > 0
        && numRandomFeaturesToChoose < numFeatures) {
      TIntHashSet set = new TIntHashSet(possibleFeatureIndices);
      // we now remove the difference from the set
      Random rnd = new Random(seed);
      while (set.size() > numRandomFeaturesToChoose) {
        set.remove(rnd.nextInt(numFeatures));
      }
      return set;
    }
    return possibleFeatureIndices;
  }

  /**
   * Recursively build the decision tree in a top down fashion.
   */
  private TreeNode build(List<DoubleVector> features,
      List<DoubleVector> outcome, TIntHashSet possibleFeatureIndices) {

    // we select a subset of features at every tree level
    possibleFeatureIndices = chooseRandomFeatures(possibleFeatureIndices);

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
      infoGain[featureIndex] = computeSplit(targetEntropy, featureIndex,
          countOutcomeClasses, features, outcome);
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
    if (featureTypes[bestSplitIndex].isNominal()) {
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
    } else {
      // numerical split
      TIntHashSet newPossibleFeatures = new TIntHashSet(possibleFeatureIndices);
      Tuple<List<DoubleVector>, List<DoubleVector>> filterNumeric = filterNumeric(
          features, outcome, bestSplitIndex,
          bestSplit.getNumericalSplitValue(), true);
      Tuple<List<DoubleVector>, List<DoubleVector>> filterNumericHigher = filterNumeric(
          features, outcome, bestSplitIndex,
          bestSplit.getNumericalSplitValue(), false);

      if (filterNumeric.getFirst().isEmpty()
          || filterNumericHigher.getFirst().isEmpty()) {
        newPossibleFeatures.remove(bestSplitIndex);
      } else {
        // we changed something, thus we can unselect all numerical features
        for (int i = 0; i < featureTypes.length; i++) {
          if (featureTypes[i].isNumerical()) {
            newPossibleFeatures.add(i);
          }
        }
      }

      // build subtrees
      TreeNode lower = build(filterNumeric.getFirst(),
          filterNumeric.getSecond(), new TIntHashSet(newPossibleFeatures));
      TreeNode higher = build(filterNumericHigher.getFirst(),
          filterNumericHigher.getSecond(), new TIntHashSet(newPossibleFeatures));
      // now we can return this completed node
      return new NumericalNode(bestSplitIndex,
          bestSplit.getNumericalSplitValue(), lower, higher);
    }

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
   * Filters the lists by numerical decision.
   * 
   * @param features the features to filter.
   * @param outcome the outcome to filter.
   * @param bestSplitIndex the feature split index with highest information
   *          gain.
   * @param splitValue the value of the split point.
   * @param lower true if the returned list should contain lower items, else it
   *          contains strictly higher items.
   * @return two filtered parallel lists.
   */
  private Tuple<List<DoubleVector>, List<DoubleVector>> filterNumeric(
      List<DoubleVector> features, List<DoubleVector> outcome,
      int bestSplitIndex, double splitValue, boolean lower) {

    List<DoubleVector> newFeatures = Lists.newLinkedList();
    List<DoubleVector> newOutcomes = Lists.newLinkedList();

    Iterator<DoubleVector> featureIterator = features.iterator();
    Iterator<DoubleVector> outcomeIterator = outcome.iterator();
    while (featureIterator.hasNext()) {
      DoubleVector feature = featureIterator.next();
      DoubleVector out = outcomeIterator.next();
      if (lower) {
        if (feature.get(bestSplitIndex) <= splitValue) {
          newFeatures.add(feature);
          newOutcomes.add(out);
        }
      } else {
        if (feature.get(bestSplitIndex) > splitValue) {
          newFeatures.add(feature);
          newOutcomes.add(out);
        }
      }

    }

    return new Tuple<>(newFeatures, newOutcomes);
  }

  /**
   * Computes the split of nominal and numerical values.
   * 
   * @param overallEntropy the overall entropy at the given time.
   * @param featureIndex the feature index to evaluate on.
   * @param countOutcomeClasses the histogram over all possible outcome classes.
   * @param features the features.
   * @param outcome the outcome.
   * @return a {@link Split} that contains a possible split (either numerical or
   *         categorical) along with the information gain.
   */
  private Split computeSplit(double overallEntropy, int featureIndex,
      int[] countOutcomeClasses, List<DoubleVector> features,
      List<DoubleVector> outcome) {

    if (featureTypes[featureIndex].isNominal()) {
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
    } else {
      // numerical case
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
            featureIndex, value);
        if (ig > bestInfogain) {
          bestInfogain = ig;
          bestSplit = value;
        }
      }
      return new Split(featureIndex, bestInfogain, bestSplit);
    }

  }

  /**
   * This method computes the numerical information gain for the given features
   * and outcomes and a featureIndex and its value. This is done by iterating
   * once over all features and outcomes and calculating a table of outcome
   * counts given a higher/lower relationship to the given feature value.
   * 
   * @param features the features.
   * @param outcome the outcomes.
   * @param overallEntropy the overall entropy of the selectable features.
   * @param featureIndex the feature index to check.
   * @param value the value that acts as a possible split point between a lower
   *          and higher partition for a given feature.
   * @return the information gain under the feature given a value as a split
   *         point.
   */
  private double computeNumericalInfogain(List<DoubleVector> features,
      List<DoubleVector> outcome, double overallEntropy, int featureIndex,
      double value) {
    double invDatasize = 1d / features.size();
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

  /**
   * @return the class index, this takes binary classification into account as
   *         well as multi class classification.
   */
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
   * Sets the type of feature per index. This should match the inputted number
   * of features in the training method. If this isn't set at all, all
   * attributes are assumed to be nominal.
   * 
   * @return this decision tree instance.
   */
  public DecisionTree setFeatureTypes(FeatureType[] featureTypes) {
    this.featureTypes = featureTypes;
    return this;
  }

  /**
   * Sets the number of random features to choose from all features.Zero,
   * negative numbers or numbers greater than the really available features
   * indicate all features to be used.
   * 
   * @return this decision tree instance.
   */
  public DecisionTree setNumRandomFeaturesToChoose(int numRandomFeaturesToChoose) {
    this.numRandomFeaturesToChoose = numRandomFeaturesToChoose;
    return this;
  }

  /**
   * Sets the seed for a random number generator if used.
   */
  public void setSeed(long seed) {
    this.seed = seed;
  }

  /*
   * for testing
   */
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
    return possibleFeatureIndices;
  }

  /**
   * @return a default decision tree with all features beeing nominal.
   */
  public static DecisionTree create() {
    return new DecisionTree();
  }

  /**
   * Creates a new decision tree with the given feature types.
   * 
   * @param featureTypes the types of the feature that must match the number of
   *          features in length.
   * @return a default decision tree with all features beeing set to what has
   *         been configured in the given array.
   */
  public static DecisionTree create(FeatureType[] featureTypes) {
    return new DecisionTree().setFeatureTypes(featureTypes);
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
