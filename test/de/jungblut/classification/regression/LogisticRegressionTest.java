package de.jungblut.classification.regression;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import de.jungblut.classification.eval.Evaluator;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.reader.CsvDatasetReader;
import de.jungblut.reader.Dataset;

public class LogisticRegressionTest {

  private static DoubleVector[] features;
  private static DoubleVector[] outcome;

  static {
    Dataset readCsv = CsvDatasetReader.readCsv("files/logreg/ex2data1.txt",
        ',', null, 2, 2);
    features = readCsv.getFeatures();
    outcome = readCsv.getOutcomes();
    double[] classes = new double[outcome.length];
    for (int i = 0; i < outcome.length; i++) {
      classes[i] = outcome[i].get(0);
    }
  }

  @Test
  public void testRegressionInterface() {
    LogisticRegression clf = new LogisticRegression(0d, new Fmincg(), 1000,
        false);
    clf.setRandom(new Random(0));
    clf.train(features, outcome);
    double trainingError = 0d;
    for (int i = 0; i < features.length; i++) {
      int predict = clf.predictedClass(features[i], 0.5d);
      trainingError += Math.abs(outcome[i].get(0) - predict);
    }
    assertEquals("Training error was: " + trainingError
        + " and should have been between 9 and 13.", 11, trainingError, 2d);
  }

  @Test
  public void testRegressionInterfaceRegularized() {
    LogisticRegression clf = new LogisticRegression(0.1d, new Fmincg(), 1000,
        false);
    clf.setRandom(new Random(0));
    clf.train(features, outcome);
    double trainingError = 0d;
    for (int i = 0; i < features.length; i++) {
      int predict = clf.predictedClass(features[i], 0.5d);
      trainingError += Math.abs(outcome[i].get(0) - predict);
    }
    assertEquals("Training error was: " + trainingError
        + " and should have been between 9 and 13!", 11, trainingError, 2d);
  }

  @Test
  public void testRegressionEvaluation() {
    LogisticRegression clf = new LogisticRegression(0d, new Fmincg(), 1000,
        false);
    clf.setRandom(new Random(0));
    EvaluationResult eval = Evaluator.evaluateClassifier(clf, features,
        outcome, 2, 0.9f, false, 0.5d);
    assertEquals(1d, eval.getPrecision(), 1e-4);
    assertEquals(10, eval.getTestSize());
    assertEquals(90, eval.getTrainSize());
  }
}
