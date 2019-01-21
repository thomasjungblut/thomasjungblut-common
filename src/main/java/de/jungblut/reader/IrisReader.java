package de.jungblut.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.base.Preconditions;

import de.jungblut.classification.eval.EvaluationSplit;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Dataset vectorizer for the iris dataset. Following outcome indices are used:
 * Iris-setosa = 0, Iris-versicolor = 1, Iris-virginica = 2.
 */
public final class IrisReader {

  private IrisReader() {
    throw new IllegalAccessError();
  }

  /**
   * @return a tuple, on first dimension are the features, on the second are the
   *         outcomes (0 or 1 in the first element of a vector)
   */
  public static Dataset readIrisDataset(String path) {
    DoubleVector[] features = new DoubleVector[150];
    DenseDoubleVector[] outcome = new DenseDoubleVector[150];

    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String line = null;
      int index = 0;
      while ((line = br.readLine()) != null) {
        String[] split = line.split(",");
        Preconditions.checkArgument(split.length == 5,
            "CSV length was not 5! Given " + split.length);
        features[index] = new DenseDoubleVector(4);
        for (int i = 0; i < split.length - 1; i++) {
          features[index].set(i, Double.parseDouble(split[i]));
        }
        if (split[split.length - 1].equals("Iris-setosa")) {
          outcome[index] = new DenseDoubleVector(new double[] { 1, 0, 0 });
        } else if (split[split.length - 1].equals("Iris-versicolor")) {
          outcome[index] = new DenseDoubleVector(new double[] { 0, 1, 0 });
        } else {
          outcome[index] = new DenseDoubleVector(new double[] { 0, 0, 1 });
        }
        index++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new Dataset(features, outcome);
  }

  /**
   * @return a split that has 40 train examples for every class and 10 test
   *         items.
   */
  public static EvaluationSplit getEvaluationSplit(Dataset irisData) {

    DoubleVector[] trainFeatures = new DenseDoubleVector[3 * 40];
    DoubleVector[] trainOutcomes = new DenseDoubleVector[3 * 40];
    DoubleVector[] testFeatures = new DenseDoubleVector[3 * 10];
    DoubleVector[] testOutcomes = new DenseDoubleVector[3 * 10];

    DoubleVector[] features = irisData.getFeatures();
    DoubleVector[] outcomes = irisData.getOutcomes();

    int[] trainSplitPoints = new int[] { 40, 90, 140 };
    int[] trainDestStartPoints = new int[] { 0, 40, 80 };
    int[] testDestStartPoints = new int[] { 0, 10, 20 };

    for (int i = 0; i < 3; i++) {
      int splitPoint = trainSplitPoints[i];
      System.arraycopy(features, splitPoint - 40, trainFeatures,
          trainDestStartPoints[i], 40);
      System.arraycopy(outcomes, splitPoint - 40, trainOutcomes,
          trainDestStartPoints[i], 40);

      System.arraycopy(features, splitPoint, testFeatures,
          testDestStartPoints[i], 10);
      System.arraycopy(outcomes, splitPoint, testOutcomes,
          testDestStartPoints[i], 10);
    }

    return new EvaluationSplit(trainFeatures, trainOutcomes, testFeatures,
        testOutcomes);
  }

}
