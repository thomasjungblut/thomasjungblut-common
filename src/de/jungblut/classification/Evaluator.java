package de.jungblut.classification;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Multi-class evaluator utility that takes care of test/train splitting and its
 * evaluation.
 * 
 * @author thomas.jungblut
 * 
 */
public final class Evaluator {

  public static class EvaluationResult {
    int numLabels, correct, trainSize, testSize, truePositive, falsePositive,
        trueNegative, falseNegative;

    public double getPrecision() {
      return ((double) truePositive) / (truePositive + falsePositive);
    }

    public double getRecall() {
      return ((double) truePositive) / (truePositive + falseNegative);
    }

    public double getAccuracy() {
      if (isBinary()) {
        return ((double) truePositive + falseNegative)
            / (truePositive + trueNegative + falsePositive + falseNegative);
      } else {
        return correct / (double) testSize;
      }
    }

    public double getF1Score() {
      return 2d * (getPrecision() * getRecall())
          / (getPrecision() + getRecall());
    }

    public int getCorrect() {
      if (!isBinary()) {
        return correct;
      } else {
        return truePositive + falseNegative;
      }
    }

    public int getNumLabels() {
      return numLabels;
    }

    public int getTrainSize() {
      return trainSize;
    }

    public int getTestSize() {
      return testSize;
    }

    public boolean isBinary() {
      return numLabels == 2;
    }

    public void print() {
      System.out.println("Number of labels: " + getNumLabels());
      System.out.println("Trainingset size: " + getTrainSize());
      System.out.println("Testset size: " + getTestSize());
      System.out.println("Correctly classified: " + getCorrect());
      System.out.println("Accuracy: " + getAccuracy());
      if (isBinary()) {
        System.out.println("TP: " + truePositive);
        System.out.println("FP: " + falsePositive);
        System.out.println("TN: " + trueNegative);
        System.out.println("FN: " + falseNegative);
        System.out.println("Precision: " + getPrecision());
        System.out.println("Recall: " + getRecall());
        System.out.println("F1 Score: " + getF1Score());
      }
    }
  }

  /**
   * Trains and evaluates the given classifier with a test split.
   * 
   * @param classifier the classifier to train and evaluate.
   * @param features the features to split.
   * @param outcome the outcome to split.
   * @param numLabels the number of labels that are used. (e.G. 2 in binary
   *          classification).
   * @param splitPercentage a value between 0f and 1f that sets the size of the
   *          trainingset. With 1k items, a splitPercentage of 0.9f will result
   *          in 900 items to train and 100 to evaluate.
   * @param random true if you want to perform shuffling on the data beforehand.
   * @return a new {@link EvaluationResult}.
   */
  public static EvaluationResult evaluateClassifier(Classifier classifier,
      DoubleVector[] features, DenseDoubleVector[] outcome, int numLabels,
      float splitPercentage, boolean random) {
    return evaluateClassifier(classifier, features, outcome, numLabels,
        splitPercentage, random, null);
  }

  /**
   * Trains and evaluates the given classifier with a test split.
   * 
   * @param classifier the classifier to train and evaluate.
   * @param features the features to split.
   * @param outcome the outcome to split.
   * @param numLabels the number of labels that are used. (e.G. 2 in binary
   *          classification).
   * @param splitPercentage a value between 0f and 1f that sets the size of the
   *          trainingset. With 1k items, a splitPercentage of 0.9f will result
   *          in 900 items to train and 100 to evaluate.
   * @param random true if you want to perform shuffling on the data beforehand.
   * @param threshold in case of binary predictions, threshold is used to call
   *          in {@link Classifier#getPredictedClass(DoubleVector, double)}. Can
   *          be null, then no thresholding will be used.
   * @return a new {@link EvaluationResult}.
   */
  public static EvaluationResult evaluateClassifier(Classifier classifier,
      DoubleVector[] features, DenseDoubleVector[] outcome, int numLabels,
      float splitPercentage, boolean random, Double threshold) {

    Preconditions.checkArgument(numLabels > 1,
        "The number of labels should be greater than 1!");
    Preconditions.checkArgument(features.length == outcome.length,
        "Feature vector and outcome vector must match in length!");

    EvaluationResult result = new EvaluationResult();
    result.numLabels = numLabels;

    if (random) {
      ArrayUtils.multiShuffle(features, outcome);
    }

    final int splitIndex = (int) (features.length * splitPercentage);
    DoubleVector[] trainFeatures = ArrayUtils.subArray(features, splitIndex);
    DenseDoubleVector[] trainOutcome = ArrayUtils.subArray(outcome, splitIndex);
    DoubleVector[] testFeatures = ArrayUtils.subArray(features, splitIndex + 1,
        features.length - 1);
    DenseDoubleVector[] testOutcome = ArrayUtils.subArray(outcome,
        splitIndex + 1, outcome.length - 1);

    result.testSize = testOutcome.length;
    result.trainSize = trainOutcome.length;

    classifier.train(trainFeatures, trainOutcome);

    // check the binary case to calculate special metrics
    if (numLabels == 2) {
      for (int i = 0; i < testFeatures.length; i++) {
        int outcomeClass = ((int) testOutcome[i].get(0));
        int prediction = 0;
        if (threshold == null) {
          prediction = classifier.getPredictedClass(testFeatures[i]);
        } else {
          prediction = classifier.getPredictedClass(testFeatures[i], threshold);
        }
        if (outcomeClass == 1) {
          if (outcomeClass == prediction) {
            result.truePositive++;
          } else {
            result.trueNegative++;
          }
        } else {
          if (outcomeClass == prediction) {
            result.falseNegative++;
          } else {
            result.falsePositive++;
          }
        }
      }
    } else {
      for (int i = 0; i < testFeatures.length; i++) {
        int outcomeClass = testOutcome[i].maxIndex();
        int prediction = classifier.getPredictedClass(testFeatures[i]);
        if (outcomeClass == prediction) {
          result.correct++;
        }
      }
      // TODO confusion matrix
    }

    return result;
  }
  // TODO cross validation with a new instance of the classifier everytime
  // (factory interface)?

}
