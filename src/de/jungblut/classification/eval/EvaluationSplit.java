package de.jungblut.classification.eval;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;

/**
 * Split data class that contains the division of train/test vectors. If no
 * division is yet there, you can use the
 * {@link #create(DoubleVector[], DoubleVector[], float, boolean)} method.
 * 
 * @author thomas.jungblut
 * 
 */
public class EvaluationSplit {

  private static final Log LOG = LogFactory.getLog(EvaluationSplit.class);

  private final DoubleVector[] trainFeatures;
  private final DoubleVector[] trainOutcome;
  private final DoubleVector[] testFeatures;
  private final DoubleVector[] testOutcome;

  /**
   * Sets a split internally.
   */
  public EvaluationSplit(DoubleVector[] trainFeatures,
      DoubleVector[] trainOutcome, DoubleVector[] testFeatures,
      DoubleVector[] testOutcome) {
    this.trainFeatures = trainFeatures;
    this.trainOutcome = trainOutcome;
    this.testFeatures = testFeatures;
    this.testOutcome = testOutcome;
  }

  public DoubleVector[] getTrainFeatures() {
    return this.trainFeatures;
  }

  public DoubleVector[] getTrainOutcome() {
    return this.trainOutcome;
  }

  public DoubleVector[] getTestFeatures() {
    return this.testFeatures;
  }

  public DoubleVector[] getTestOutcome() {
    return this.testOutcome;
  }

  /**
   * Creates a new evaluation split.
   * 
   * @param features the features of your classifier.
   * @param outcome the target variables of the classifier.
   * @param splitFraction a value between 0f and 1f that sets the size of the
   *          trainingset. With 1k items, a splitPercentage of 0.9f will result
   *          in 900 items to train and 100 to evaluate.
   * @param random true if data needs shuffling before.
   * @return a new {@link EvaluationSplit}.
   */
  public static EvaluationSplit create(DoubleVector[] features,
      DoubleVector[] outcome, float splitFraction, boolean random) {
    Preconditions.checkArgument(features.length == outcome.length,
        "Feature vector and outcome vector must match in length!");
    Preconditions.checkArgument(splitFraction >= 0f && splitFraction <= 1f,
        "splitFraction must be between 0 and 1! Given: " + splitFraction);

    if (random) {
      ArrayUtils.multiShuffle(features, outcome);
    }

    final int splitIndex = (int) (features.length * splitFraction);
    DoubleVector[] trainFeatures = ArrayUtils
        .subArray(features, splitIndex - 1);
    DoubleVector[] trainOutcome = ArrayUtils.subArray(outcome, splitIndex - 1);
    DoubleVector[] testFeatures = ArrayUtils.subArray(features, splitIndex,
        features.length - 1);
    DoubleVector[] testOutcome = ArrayUtils.subArray(outcome, splitIndex,
        outcome.length - 1);
    return new EvaluationSplit(trainFeatures, trainOutcome, testFeatures,
        testOutcome);
  }

  /**
   * Creates a new stratified evaluation split. Sampling is done based on the
   * max index of the outcome classes (assumes one-hot encoding, or zero/one
   * encoding for binary classes). This class does not keep the relation of the
   * original outcome vectors to their features, thus every mutual information
   * stored for both should be included in the feature vector.
   * 
   * @param features the features of your classifier.
   * @param outcome the target variables of the classifier.
   * @param splitFraction a value between 0f and 1f that sets the size of the
   *          trainingset. With 1k items, a splitPercentage of 0.9f will result
   *          in 900 items to train and 100 to evaluate.
   * @param random true if data needs shuffling before.
   * @return a new {@link EvaluationSplit}.
   */
  public static EvaluationSplit createStratified(DoubleVector[] features,
      DoubleVector[] outcome, float splitFraction, boolean random) {
    Preconditions.checkArgument(features.length == outcome.length,
        "Feature vector and outcome vector must match in length!");
    Preconditions.checkArgument(splitFraction >= 0f && splitFraction <= 1f,
        "splitFraction must be between 0 and 1! Given: " + splitFraction);

    // we group features based on their class

    @SuppressWarnings("unchecked")
    Deque<DoubleVector>[] multiQueues = new Deque[Math.max(2,
        outcome[0].getDimension())];

    DoubleVector[] sampleOutcome = new DoubleVector[multiQueues.length];

    for (int i = 0; i < features.length; i++) {
      int key = multiQueues.length == 2 ? (int) outcome[i].get(0) : outcome[i]
          .maxIndex();
      Deque<DoubleVector> deque = multiQueues[key];
      if (deque == null) {
        deque = new LinkedList<>();
        multiQueues[key] = deque;
      }
      deque.addLast(features[i]);
      sampleOutcome[key] = outcome[i];
    }

    double[] samplingProbabilities = new double[multiQueues.length];
    for (int i = 0; i < multiQueues.length; i++) {
      samplingProbabilities[i] = ((double) multiQueues[i].size())
          / features.length;
    }

    LOG.info("Sampling probabilities by class: "
        + Arrays.toString(samplingProbabilities));

    final int splitSize = (int) (features.length * splitFraction);
    DoubleVector[] trainFeatures = new DoubleVector[splitSize];
    DoubleVector[] trainOutcomes = new DoubleVector[splitSize];
    int offset = 0;
    for (int s = 0; s < samplingProbabilities.length; s++) {
      int fractionIndex = (int) (splitSize * samplingProbabilities[s]);
      for (int i = 0; i < fractionIndex; i++) {
        trainFeatures[offset] = multiQueues[s].poll();
        trainOutcomes[offset] = sampleOutcome[s];
        offset++;
      }
    }

    // poll the rest out of the queues for the test set
    DoubleVector[] testFeatures = new DoubleVector[features.length - splitSize];
    DoubleVector[] testOutcomes = new DoubleVector[features.length - splitSize];

    offset = 0;
    for (int i = 0; i < multiQueues.length; i++) {
      while (!multiQueues[i].isEmpty()) {
        if (offset >= testFeatures.length) {
          throw new IllegalArgumentException(
              "Splits couldn't be fully stratified! One class must've been too small to be splittable.");
        }
        testFeatures[offset] = multiQueues[i].poll();
        testOutcomes[offset] = sampleOutcome[i];
        offset++;
      }
    }

    if (random) {
      // radomize both sets again
      ArrayUtils.multiShuffle(trainFeatures, trainOutcomes);
      ArrayUtils.multiShuffle(testFeatures, testOutcomes);
    }

    return new EvaluationSplit(trainFeatures, trainOutcomes, testFeatures,
        testOutcomes);
  }
}
