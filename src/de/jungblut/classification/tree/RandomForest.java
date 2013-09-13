package de.jungblut.classification.tree;

import java.util.Arrays;

import com.google.common.base.Preconditions;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.classification.ClassifierFactory;
import de.jungblut.classification.meta.Voter;
import de.jungblut.classification.meta.Voter.CombiningType;
import de.jungblut.classification.meta.Voter.SelectionType;
import de.jungblut.math.DoubleVector;

/**
 * A decision tree forest, using bagging. The decision trees inside are compiled
 * directly into byte code for fast performance. The training can be done
 * multithreaded.
 * 
 * @author thomasjungblut
 * 
 */
public final class RandomForest extends AbstractClassifier {

  private final int numTrees;
  private FeatureType[] featureTypes;
  private int numThreads = 1;
  private int numRandomFeaturesToChoose = 0;
  private boolean verbose;
  private Voter<DecisionTree> trees;

  private RandomForest(int numTrees) {
    this.numTrees = numTrees;
  }

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    Preconditions.checkArgument(features.length == outcome.length,
        "Number of examples and outcomes must match!");
    Preconditions.checkArgument(numTrees > 1,
        "There must be at least two trees to make up a forest!");
    // assume all nominal if nothing was set
    if (featureTypes == null) {
      featureTypes = new FeatureType[features[0].getDimension()];
      Arrays.fill(featureTypes, FeatureType.NOMINAL);
    }
    int numFeatures = features[0].getDimension();
    if (numRandomFeaturesToChoose <= 0) {
      numRandomFeaturesToChoose = (int) Math.sqrt(numFeatures);
    }
    Preconditions.checkArgument(
        featureTypes.length == features[0].getDimension(),
        "FeatureType length must match the dimension of the features!");
    Preconditions.checkArgument(numRandomFeaturesToChoose < numFeatures,
        "Number of random features to choose must be "
            + "lower or equal than the number of features!");

    trees = Voter
        .create(numTrees, CombiningType.MAJORITY, new DecisionTreeFactory())
        .selectionType(SelectionType.BAGGING).numThreads(numThreads)
        .verbose(verbose);
    // do the training!
    trees.train(features, outcome);
  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    // just proxy to the voter
    return trees.predict(features);
  }

  @Override
  public DoubleVector predictProbability(DoubleVector features) {
    trees.setCombiningType(CombiningType.PROBABILITY);
    return predict(features);
  }

  /**
   * @return sets this instance to verbose and returns it.
   */
  public RandomForest verbose() {
    return verbose(true);
  }

  /**
   * @return sets this instance to verbose and returns it.
   */
  public RandomForest verbose(boolean verb) {
    this.verbose = verb;
    return this;
  }

  /**
   * @return this instance, set the number of threads for training the forest.
   */
  public RandomForest numThreads(int numThreads) {
    this.numThreads = numThreads;
    return this;
  }

  /**
   * @return this instance, set to the number of random features to choose at
   *         every decision tree level.
   */
  public RandomForest setNumRandomFeaturesToChoose(int numRandomFeaturesToChoose) {
    this.numRandomFeaturesToChoose = numRandomFeaturesToChoose;
    return this;
  }

  /**
   * @return sets the feature types of the decision tree.
   */
  public RandomForest setFeatureTypes(FeatureType[] types) {
    this.featureTypes = types;
    return this;
  }

  /**
   * Creates a new random forest, trains on one thread with the number of trees
   * supplied. It chooses sqrt(#features) random features at each tree level. In
   * addition, it treats all features as categorical values.
   */
  public static RandomForest create(int numTrees) {
    return new RandomForest(numTrees);
  }

  /**
   * Creates a new random forest, trains on one thread with the number of trees
   * supplied. It chooses log(#features) random features at each tree level.
   */
  public static RandomForest create(int numTrees, FeatureType[] types) {
    return new RandomForest(numTrees).setFeatureTypes(types);
  }

  private final class DecisionTreeFactory implements
      ClassifierFactory<DecisionTree> {

    @Override
    public DecisionTree newInstance() {
      return DecisionTree.createCompiledTree(featureTypes)
          .setNumRandomFeaturesToChoose(numRandomFeaturesToChoose);
    }

  }

}
