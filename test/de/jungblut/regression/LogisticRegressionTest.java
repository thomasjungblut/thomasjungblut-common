package de.jungblut.regression;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.classification.regression.LogisticRegression;
import de.jungblut.classification.regression.LogisticRegressionCostFunction;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;

public class LogisticRegressionTest extends TestCase {

  List<String> readAllLines;
  {
    try {
      // this file is from ml-class second exercise to verify it is working
      // correct
      readAllLines = Files.readAllLines(
          FileSystems.getDefault().getPath("files/logreg/ex2data1.txt"),
          Charset.defaultCharset());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  DenseDoubleMatrix x = new DenseDoubleMatrix(readAllLines.size(), 2);
  DenseDoubleVector y = new DenseDoubleVector(readAllLines.size());
  {
    for (int i = 0; i < readAllLines.size(); i++) {
      String line = readAllLines.get(i);
      String[] split = line.split(",");
      x.set(i, 0, Double.parseDouble(split[0]));
      x.set(i, 1, Double.parseDouble(split[1]));
      y.set(i, Integer.parseInt(split[2]));
    }
  }

  @Test
  public void testLogisticRegression() {

    LogisticRegressionCostFunction fnc = new LogisticRegressionCostFunction(x,
        y, 1d);

    DoubleVector theta = Fmincg.minimizeFunction(fnc, new DenseDoubleVector(
        new double[] { 0, 0, 0 }), 1000, false);

    assertEquals(-25.052165981708658, theta.get(0));
    assertEquals(0.20535460559228136, theta.get(1));
    assertEquals(0.20058370043792928, theta.get(2));
  }

  @Test
  public void testPredictions() {
    LogisticRegression reg = new LogisticRegression(1.0d, new Fmincg(), 1000,
        false);
    reg.train(x, y);
    DoubleVector predict = reg.predict(x, 0.5d);

    double wrongPredictions = predict.subtract(y).abs().sum();
    assertEquals(11.0d, wrongPredictions);
    double trainAccuracy = (y.getLength() - wrongPredictions) / y.getLength();

    assertTrue(trainAccuracy > 0.85);
  }
}
