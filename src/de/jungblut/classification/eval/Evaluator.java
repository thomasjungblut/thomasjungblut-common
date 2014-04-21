package de.jungblut.classification.eval;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.jungblut.classification.Classifier;
import de.jungblut.classification.ClassifierFactory;
import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.partition.BlockPartitioner;
import de.jungblut.partition.Boundaries.Range;

/**
 * Binary-/Multi-class classification evaluator utility that takes care of
 * test/train splitting and its evaluation with various metrics.
 * 
 * @author thomas.jungblut
 * 
 */
public final class Evaluator {

  private static final Log LOG = LogFactory.getLog(Evaluator.class);

  private Evaluator() {
    throw new IllegalAccessError();
  }

  public static class EvaluationResult {
    int numLabels, correct, trainSize, testSize, truePositive, falsePositive,
        trueNegative, falseNegative;
    int[][] confusionMatrix;
    double auc;

    public double getAUC() {
      return auc;
    }

    public double getPrecision() {
      return ((double) truePositive) / (truePositive + falsePositive);
    }

    public double getRecall() {
      return ((double) truePositive) / (truePositive + falseNegative);
    }

    public double getAccuracy() {
      if (isBinary()) {
        return ((double) truePositive + trueNegative)
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
        return truePositive + trueNegative;
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

    public int[][] getConfusionMatrix() {
      return this.confusionMatrix;
    }

    public boolean isBinary() {
      return numLabels == 2;
    }

    public void add(EvaluationResult res) {
      correct += res.correct;
      trainSize += res.trainSize;
      testSize += res.testSize;
      truePositive += res.truePositive;
      falsePositive += res.falsePositive;
      trueNegative += res.trueNegative;
      falseNegative += res.falseNegative;
      auc += res.auc;
      if (this.confusionMatrix == null && res.confusionMatrix != null) {
        this.confusionMatrix = res.confusionMatrix;
      } else if (this.confusionMatrix != null && res.confusionMatrix != null) {
        for (int i = 0; i < numLabels; i++) {
          for (int j = 0; j < numLabels; j++) {
            this.confusionMatrix[i][j] += res.confusionMatrix[i][j];
          }
        }
      }
    }

    public void average(int pn) {
      final double n = pn;
      correct /= n;
      trainSize /= n;
      testSize /= n;
      truePositive /= n;
      falsePositive /= n;
      trueNegative /= n;
      falseNegative /= n;
      auc /= n;
      if (this.confusionMatrix != null) {
        for (int i = 0; i < numLabels; i++) {
          for (int j = 0; j < numLabels; j++) {
            this.confusionMatrix[i][j] /= n;
          }
        }
      }
    }

    public int getTruePositive() {
      return this.truePositive;
    }

    public int getFalsePositive() {
      return this.falsePositive;
    }

    public int getTrueNegative() {
      return this.trueNegative;
    }

    public int getFalseNegative() {
      return this.falseNegative;
    }

    public void print() {
      print(LOG);
    }

    public void print(Log log) {
      log.info("Number of labels: " + getNumLabels());
      log.info("Trainingset size: " + getTrainSize());
      log.info("Testset size: " + getTestSize());
      log.info("Correctly classified: " + getCorrect());
      log.info("Accuracy: " + getAccuracy());
      if (isBinary()) {
        log.info("TP: " + truePositive);
        log.info("FP: " + falsePositive);
        log.info("TN: " + trueNegative);
        log.info("FN: " + falseNegative);
        log.info("Precision: " + getPrecision());
        log.info("Recall: " + getRecall());
        log.info("F1 Score: " + getF1Score());
        log.info("AUC: " + getAUC());
      } else {
        printConfusionMatrix();
      }
    }

    public void printConfusionMatrix() {
      printConfusionMatrix(null);
    }

    public void printConfusionMatrix(String[] classNames) {
      Preconditions.checkNotNull(this.confusionMatrix,
          "No confusion matrix found.");

      System.out
          .println("\nConfusion matrix (real outcome on rows, prediction in columns)\n");
      for (int i = 0; i < getNumLabels(); i++) {
        System.out.format("%5d", i);
      }
      System.out.format(" <- %5s %5s\t%s\n", "sum", "perc", "class");

      for (int i = 0; i < getNumLabels(); i++) {
        int sum = 0;
        for (int j = 0; j < getNumLabels(); j++) {
          if (i != j) {
            sum += confusionMatrix[i][j];
          }
          System.out.format("%5d", confusionMatrix[i][j]);
        }
        float falsePercentage = sum / (float) (sum + confusionMatrix[i][i]);
        String clz = classNames != null ? " " + i + " (" + classNames[i] + ")"
            : " " + i;
        System.out.format(" <- %5s %5s\t%s\n", sum, NumberFormat
            .getPercentInstance().format(falsePercentage), clz);
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
   * @param splitFraction a value between 0f and 1f that sets the size of the
   *          trainingset. With 1k items, a splitFraction of 0.9f will result in
   *          900 items to train and 100 to evaluate.
   * @param random true if you want to perform shuffling on the data beforehand.
   * @return a new {@link EvaluationResult}.
   */
  public static EvaluationResult evaluateClassifier(Classifier classifier,
      DoubleVector[] features, DoubleVector[] outcome, int numLabels,
      float splitFraction, boolean random) {
    return evaluateClassifier(classifier, features, outcome, numLabels,
        splitFraction, random, null);
  }

  /**
   * Trains and evaluates the given classifier with a test split.
   * 
   * @param classifier the classifier to train and evaluate.
   * @param features the features to split.
   * @param outcome the outcome to split.
   * @param numLabels the number of labels that are used. (e.G. 2 in binary
   *          classification).
   * @param splitFraction a value between 0f and 1f that sets the size of the
   *          trainingset. With 1k items, a splitFraction of 0.9f will result in
   *          900 items to train and 100 to evaluate.
   * @param random true if you want to perform shuffling on the data beforehand.
   * @param threshold in case of binary predictions, threshold is used to call
   *          in {@link Classifier#predictedClass(DoubleVector, double)}. Can be
   *          null, then no thresholding will be used.
   * @return a new {@link EvaluationResult}.
   */
  public static EvaluationResult evaluateClassifier(Classifier classifier,
      DoubleVector[] features, DoubleVector[] outcome, int numLabels,
      float splitFraction, boolean random, Double threshold) {

    Preconditions.checkArgument(numLabels > 1,
        "The number of labels should be greater than 1!");

    EvaluationSplit split = EvaluationSplit.create(features, outcome,
        splitFraction, random);

    return evaluateSplit(classifier, numLabels, threshold, split);
  }

  /**
   * Evaluates a given train/test split with the given classifier.
   * 
   * @param classifier the classifier to train on the train split.
   * @param numLabels the number of labels that can be classified.
   * @param threshold the threshold for predicting a specific class by
   *          probability (if not provided = null).
   * @param split the {@link EvaluationSplit} that contains the test and train
   *          data.
   * @return a fresh evalation result filled with the evaluated metrics.
   */
  public static EvaluationResult evaluateSplit(Classifier classifier,
      int numLabels, Double threshold, EvaluationSplit split) {
    return evaluateSplit(classifier, numLabels, threshold,
        split.getTrainFeatures(), split.getTrainOutcome(),
        split.getTestFeatures(), split.getTestOutcome());
  }

  /**
   * Evaluates a given train/test split with the given classifier.
   * 
   * @param classifier the classifier to train on the train split.
   * @param numLabels the number of labels that can be classified.
   * @param threshold the threshold for predicting a specific class by
   *          probability (if not provided = null).
   * @param trainFeatures the features to train with.
   * @param trainOutcome the outcomes to train with.
   * @param testFeatures the features to test with.
   * @param testOutcome the outcome to test with.
   * @return a fresh evalation result filled with the evaluated metrics.
   */
  public static EvaluationResult evaluateSplit(Classifier classifier,
      int numLabels, Double threshold, DoubleVector[] trainFeatures,
      DoubleVector[] trainOutcome, DoubleVector[] testFeatures,
      DoubleVector[] testOutcome) {

    classifier.train(trainFeatures, trainOutcome);

    return testClassifier(classifier, numLabels, threshold,
        trainOutcome.length, testFeatures, testOutcome);
  }

  /**
   * Tests the given classifier without actually training it.
   * 
   * @param classifier the classifier to evaluate on the test split.
   * @param numLabels the number of labels that can be classified.
   * @param threshold the threshold for predicting a specific class by
   *          probability (if not provided = null).
   * @param trainingSetSize the size of the training set (just for reference).
   * @param testFeatures the features to test with.
   * @param testOutcome the outcome to test with.
   * @return a fresh evalation result filled with the evaluated metrics.
   */
  public static EvaluationResult testClassifier(Classifier classifier,
      int numLabels, Double threshold, int trainingSetSize,
      DoubleVector[] testFeatures, DoubleVector[] testOutcome) {
    EvaluationResult result = new EvaluationResult();
    result.numLabels = numLabels;
    result.testSize = testOutcome.length;
    result.trainSize = trainingSetSize;
    // check the binary case to calculate special metrics
    if (numLabels == 2) {
      List<Tuple<Integer, Double>> outcomePredictedPairs = new ArrayList<>();
      for (int i = 0; i < testFeatures.length; i++) {
        int outcomeClass = ((int) testOutcome[i].get(0));
        DoubleVector predictedVector = classifier.predict(testFeatures[i]);
        outcomePredictedPairs.add(new Tuple<>(outcomeClass, predictedVector
            .get(0)));
        int prediction = 0;
        if (threshold == null) {
          prediction = classifier.extractPredictedClass(predictedVector);
        } else {
          prediction = classifier.extractPredictedClass(predictedVector,
              threshold);
        }
        if (outcomeClass == 1) {
          if (prediction == 1) {
            result.truePositive++; // "Correct result"
          } else {
            result.falseNegative++; // "Missing the correct result"
          }
        } else if (outcomeClass == 0) {
          if (prediction == 0) {
            result.trueNegative++; // "Correct absence of result"
          } else {
            result.falsePositive++; // "Unexpected result"
          }
        } else {
          throw new IllegalArgumentException(
              "Outcome class was neither 0 or 1. Was: " + outcomeClass
                  + "; the supplied outcome value was: "
                  + testOutcome[i].get(0));
        }

        // we can compute the AUC from the outcomePredictedPairs we gathered
        result.auc = computeAUC(outcomePredictedPairs);
      }
    } else {
      int[][] confusionMatrix = new int[numLabels][numLabels];
      for (int i = 0; i < testFeatures.length; i++) {
        int outcomeClass = testOutcome[i].maxIndex();
        int prediction = classifier.predictedClass(testFeatures[i]);
        confusionMatrix[outcomeClass][prediction]++;
        if (outcomeClass == prediction) {
          result.correct++;
        }
      }
      result.confusionMatrix = confusionMatrix;
    }
    return result;
  }

  /**
   * This is actually taken from Kaggle's C# implementation: {@link https
   * ://www.kaggle.com/c/SemiSupervisedFeatureLearning
   * /forums/t/919/auc-implementation/6136#post6136}.
   * 
   * Package-visible for testing reasons.
   * 
   * @param outcomePredictedPairs the list of tuples: class (0 or 1) ->
   *          predicted value
   * @return the AUC value.
   */
  static double computeAUC(List<Tuple<Integer, Double>> outcomePredictedPairs) {

    // order by the predicted value
    Collections.sort(outcomePredictedPairs,
        new Comparator<Tuple<Integer, Double>>() {
          @Override
          public int compare(Tuple<Integer, Double> o1,
              Tuple<Integer, Double> o2) {
            return Double.compare(o1.getSecond(), o2.getSecond());
          }
        });
    int n = outcomePredictedPairs.size();
    int numOnes = 0;
    for (Tuple<Integer, Double> tuple : outcomePredictedPairs) {
      if (tuple.getFirst() == 1) {
        numOnes++;
      }
    }

    if (numOnes == 0 || numOnes == n) {
      return 1d;
    }

    long tp0, tn;
    long truePos = tp0 = numOnes;
    long accum = tn = 0;
    double threshold = outcomePredictedPairs.get(0).getSecond();
    for (int i = 0; i < n; i++) {
      double actualValue = outcomePredictedPairs.get(i).getFirst();
      double predictedValue = outcomePredictedPairs.get(i).getSecond();
      if (predictedValue != threshold) { // threshold changes
        threshold = predictedValue;
        accum += tn * (truePos + tp0); // 2* the area of trapezoid
        tp0 = truePos;
        tn = 0;
      }
      tn += 1 - actualValue; // x-distance between adjacent points
      truePos -= actualValue;
    }
    accum += tn * (truePos + tp0); // 2 * the area of trapezoid
    return (double) accum / (2 * numOnes * (n - numOnes));
  }

  /**
   * Does a k-fold crossvalidation on the given classifiers with features and
   * outcomes. The folds will be calculated on a new thread.
   * 
   * @param classifierFactory the classifiers to train and test.
   * @param features the features to train/test with.
   * @param outcome the outcomes to train/test with.
   * @param numLabels the total number of labels that are possible. e.G. 2 in
   *          the binary case.
   * @param folds the number of folds to fold, usually 10.
   * @param threshold the threshold for predicting a specific class by
   *          probability (if not provided = null).
   * @param verbose true if partial fold results should be printed.
   * @return a averaged evaluation result over all k folds.
   */
  public static <A extends Classifier> EvaluationResult crossValidateClassifier(
      ClassifierFactory<A> classifierFactory, DoubleVector[] features,
      DoubleVector[] outcome, int numLabels, int folds, Double threshold,
      boolean verbose) {
    return crossValidateClassifier(classifierFactory, features, outcome,
        numLabels, folds, threshold, 1, verbose);
  }

  /**
   * Does a k-fold crossvalidation on the given classifiers with features and
   * outcomes.
   * 
   * @param classifierFactory the classifiers to train and test.
   * @param features the features to train/test with.
   * @param outcome the outcomes to train/test with.
   * @param numLabels the total number of labels that are possible. e.G. 2 in
   *          the binary case.
   * @param folds the number of folds to fold, usually 10.
   * @param threshold the threshold for predicting a specific class by
   *          probability (if not provided = null).
   * @param numThreads how many threads to use to evaluate the folds.
   * @param verbose true if partial fold results should be printed.
   * @return a averaged evaluation result over all k folds.
   */
  public static <A extends Classifier> EvaluationResult crossValidateClassifier(
      ClassifierFactory<A> classifierFactory, DoubleVector[] features,
      DoubleVector[] outcome, int numLabels, int folds, Double threshold,
      int numThreads, boolean verbose) {
    // train on k-1 folds, test on 1 fold, results are averaged
    final int numFolds = folds + 1;
    // multi shuffle the arrays first, note that this is not stratified.
    ArrayUtils.multiShuffle(features, outcome);

    EvaluationResult averagedModel = new EvaluationResult();
    averagedModel.numLabels = numLabels;
    final int m = features.length;
    // compute the split ranges by blocks, so we have range from 0 to the next
    // partition index end that will be our testset, and so on.
    List<Range> partition = new ArrayList<>(new BlockPartitioner().partition(
        numFolds, m).getBoundaries());
    int[] splitRanges = new int[numFolds];
    for (int i = 1; i < numFolds; i++) {
      splitRanges[i] = partition.get(i).getEnd();
    }

    // because we are dealing with indices, we have to subtract 1 from the end
    splitRanges[numFolds - 1] = splitRanges[numFolds - 1] - 1;

    if (verbose) {
      LOG.info("Computed split ranges: " + Arrays.toString(splitRanges) + "\n");
    }
    final ExecutorService pool = Executors.newFixedThreadPool(numThreads,
        new ThreadFactoryBuilder().setDaemon(true).build());
    final ExecutorCompletionService<EvaluationResult> completionService = new ExecutorCompletionService<>(
        pool);

    // build the models fold for fold
    for (int fold = 0; fold < folds; fold++) {
      completionService.submit(new CallableEvaluation<>(fold, splitRanges, m,
          classifierFactory, features, outcome, numLabels, folds, threshold));
    }

    // retrieve the results
    for (int fold = 0; fold < folds; fold++) {
      Future<EvaluationResult> take;
      try {
        take = completionService.take();
        EvaluationResult foldSplit = take.get();
        if (verbose) {
          LOG.info("Fold: " + (fold + 1));
          foldSplit.print();
          LOG.info("");
        }
        averagedModel.add(foldSplit);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }

    // average the sums in the model
    averagedModel.average(folds);
    return averagedModel;
  }

  /**
   * Does a 10 fold crossvalidation.
   * 
   * @param classifierFactory the classifiers to train and test.
   * @param features the features to train/test with.
   * @param outcome the outcomes to train/test with.
   * @param numLabels the total number of labels that are possible. e.G. 2 in
   *          the binary case.
   * @param threshold the threshold for predicting a specific class by
   *          probability (if not provided = null).
   * @param numThreads how many threads to use to evaluate the folds.
   * @param verbose true if partial fold results should be printed.
   * @return a averaged evaluation result over all 10 folds.
   */
  public static <A extends Classifier> EvaluationResult tenFoldCrossValidation(
      ClassifierFactory<A> classifierFactory, DoubleVector[] features,
      DoubleVector[] outcome, int numLabels, Double threshold, boolean verbose) {
    return crossValidateClassifier(classifierFactory, features, outcome,
        numLabels, 10, threshold, verbose);
  }

  /**
   * Does a 10 fold crossvalidation.
   * 
   * @param classifierFactory the classifiers to train and test.
   * @param features the features to train/test with.
   * @param outcome the outcomes to train/test with.
   * @param numLabels the total number of labels that are possible. e.G. 2 in
   *          the binary case.
   * @param threshold the threshold for predicting a specific class by
   *          probability (if not provided = null).
   * @param verbose true if partial fold results should be printed.
   * @return a averaged evaluation result over all 10 folds.
   */
  public static <A extends Classifier> EvaluationResult tenFoldCrossValidation(
      ClassifierFactory<A> classifierFactory, DoubleVector[] features,
      DoubleVector[] outcome, int numLabels, Double threshold, int numThreads,
      boolean verbose) {
    return crossValidateClassifier(classifierFactory, features, outcome,
        numLabels, 10, threshold, numThreads, verbose);
  }

  private static class CallableEvaluation<A extends Classifier> implements
      Callable<EvaluationResult> {

    private final int fold;
    private final int[] splitRanges;
    private final int m;
    private final DoubleVector[] features;
    private final DoubleVector[] outcome;
    private final ClassifierFactory<A> classifierFactory;
    private final int numLabels;
    private final Double threshold;

    public CallableEvaluation(int fold, int[] splitRanges, int m,
        ClassifierFactory<A> classifierFactory, DoubleVector[] features,
        DoubleVector[] outcome, int numLabels, int folds, Double threshold) {
      this.fold = fold;
      this.splitRanges = splitRanges;
      this.m = m;
      this.classifierFactory = classifierFactory;
      this.features = features;
      this.outcome = outcome;
      this.numLabels = numLabels;
      this.threshold = threshold;
    }

    @Override
    public EvaluationResult call() throws Exception {
      DoubleVector[] featureTest = ArrayUtils.subArray(features,
          splitRanges[fold], splitRanges[fold + 1]);
      DoubleVector[] outcomeTest = ArrayUtils.subArray(outcome,
          splitRanges[fold], splitRanges[fold + 1]);
      DoubleVector[] featureTrain = new DoubleVector[m - featureTest.length];
      DoubleVector[] outcomeTrain = new DoubleVector[m - featureTest.length];
      int index = 0;
      for (int i = 0; i < m; i++) {
        if (i < splitRanges[fold] || i > splitRanges[fold + 1]) {
          featureTrain[index] = features[i];
          outcomeTrain[index] = outcome[i];
          index++;
        }
      }

      return evaluateSplit(classifierFactory.newInstance(), numLabels,
          threshold, featureTrain, outcomeTrain, featureTest, outcomeTest);
    }

  }

}
