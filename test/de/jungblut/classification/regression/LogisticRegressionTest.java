package de.jungblut.classification.regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.jungblut.classification.Classifier;
import de.jungblut.classification.eval.Evaluator;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.reader.CsvDatasetReader;
import de.jungblut.reader.Dataset;

public class LogisticRegressionTest {

  private static DoubleVector[] features;
  private static DoubleVector[] outcome;
  private static DenseDoubleVector y;
  private static DenseDoubleMatrix x;

  static {
    LogisticRegression.SEED = 0L;
    Dataset readCsv = CsvDatasetReader.readCsv("files/logreg/ex2data1.txt",
        ',', null, 2, 2);
    features = readCsv.getFeatures();
    outcome = readCsv.getOutcomes();
    double[] classes = new double[outcome.length];
    for (int i = 0; i < outcome.length; i++) {
      classes[i] = outcome[i].get(0);
    }
    y = new DenseDoubleVector(classes);
    x = new DenseDoubleMatrix(features);
  }

  @Test
  public void testLogisticRegression() {

    LogisticRegressionCostFunction fnc = new LogisticRegressionCostFunction(
        new DenseDoubleMatrix(features), y, 1d);

    DoubleVector theta = Fmincg.minimizeFunction(fnc, new DenseDoubleVector(
        new double[] { 0, 0, 0 }), 1000, false);

    assertEquals(theta.get(0), -25.05, 0.1);
    assertEquals(theta.get(1), 0.2, 0.1);
    assertEquals(theta.get(2), 0.2, 0.1);
  }

  @Test
  public void testPredictions() {
    LogisticRegression reg = new LogisticRegression(1.0d, new Fmincg(), 1000,
        false);
    reg.train(x, y);
    DoubleVector predict = reg.predict(x, 0.5d);

    double wrongPredictions = predict.subtract(y).abs().sum();
    assertEquals(11.0d, wrongPredictions, 1e-4);
    double trainAccuracy = (y.getLength() - wrongPredictions) / y.getLength();

    assertTrue(trainAccuracy > 0.85);
  }

  @Test
  public void testRegressionInterface() {
    Classifier clf = new LogisticRegression(1.0d, new Fmincg(), 1000, false);
    clf.train(features, outcome);
    double trainingError = 0d;
    for (int i = 0; i < features.length; i++) {
      int predict = clf.getPredictedClass(features[i], 0.5d);
      trainingError += Math.abs(outcome[i].get(0) - predict);
    }
    assertEquals("Training error was: " + trainingError
        + " and should have been between 9 and 13.", trainingError, 11, 2d);
  }

  @Test
  public void testRegressionEvaluation() {
    Classifier clf = new LogisticRegression(1.0d, new Fmincg(), 1000, false);
    EvaluationResult eval = Evaluator.evaluateClassifier(clf, features,
        outcome, 2, 0.9f, false, 0.5d);
    assertEquals(1d, eval.getPrecision(), 1e-4);
    assertEquals(10, eval.getTestSize());
    assertEquals(90, eval.getTrainSize());
  }
}
