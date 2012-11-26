package de.jungblut.math;

import java.util.Iterator;

import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Viterbi Utilities for forward backward passes and his famous decoding
 * algorithm for hidden markov models.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ViterbiUtils {

  /**
   * Do a decoding pass on the given HMM weights, the features to decode and how
   * many classes to predict. The output will contain a vector that contains a 1
   * at the index of the predicted label.
   * 
   * @param weights the HMM weights.
   * @param features the features to predict on.
   * @param featuresPerState the matrix containing the feature vectors,
   *          precomputed for each possible state in classes. The layout is that
   *          the same feature was computed n-times, so class 0 first, class 1
   *          next and so on and this is layed out in rows (Feature 1 | class 0,
   *          Feature 1 | class 1 ...). Feature 0 is only contained once,
   *          because it only had class zero as previous class.
   * @param classes how many classes? 2 if binary.
   * @return a n x m matrix where n is the number of featurevectors and m is the
   *         number of classes (in binary prediction this is just 1, 0 and 1 are
   *         the predicted labels at index 0 then).
   */
  public static DoubleMatrix decode(DoubleMatrix weights,
      DoubleMatrix features, DoubleMatrix featuresPerState, int classes) {
    final int m = features.getRowCount();
    int[][] backpointers = new int[m][classes];
    double[][] scores = new double[m][classes];

    // define the starting label as 0.
    int prevLabel = 0;
    double[] localScores = computeScores(classes, features.getRowVector(0),
        weights);

    int position = 0;
    for (int currLabel = 0; currLabel < localScores.length; currLabel++) {
      backpointers[position][currLabel] = prevLabel;
      scores[position][currLabel] = localScores[currLabel];
    }

    // for each position in data
    for (position = 1; position < m; position++) {
      int i = position * classes - 1;
      // for each possible previous label
      for (int j = 0; j < classes; j++) {
        prevLabel = j;
        localScores = computeScores(classes,
            featuresPerState.getRowVector(i + j), weights);
        for (int currLabel = 0; currLabel < localScores.length; currLabel++) {
          double score = localScores[currLabel]
              + scores[position - 1][prevLabel];
          if (prevLabel == 0 || score > scores[position][currLabel]) {
            backpointers[position][currLabel] = prevLabel;
            scores[position][currLabel] = score;
          }
        }
      }
    }

    int bestLabel = 0;
    double bestScore = scores[m - 1][bestLabel];
    for (int label = 1; label < scores[m - 1].length; label++) {
      if (scores[m - 1][label] > bestScore) {
        bestLabel = label;
        bestScore = scores[m - 1][label];
      }
    }

    DoubleMatrix outcome = new DenseDoubleMatrix(features.getRowCount(),
        classes == 2 ? 1 : classes);
    // follow the backpointers
    for (position = m - 1; position >= 0; position--) {
      DenseDoubleVector vec = null;
      if (classes != 2) {
        vec = new DenseDoubleVector(classes);
        vec.set(bestLabel, 1);
      } else {
        vec = new DenseDoubleVector(1);
        vec.set(0, bestLabel);
      }
      outcome.setRowVector(position, vec);
      bestLabel = backpointers[position][bestLabel];
    }

    return outcome;
  }

  // compute the scores for a featurevector and its weighs and the number of
  // classes
  static double[] computeScores(int classes, DoubleVector features,
      DoubleMatrix weights) {

    double[] scores = new double[classes];

    Iterator<DoubleVectorElement> iterateNonZero = features.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      for (int i = 0; i < scores.length; i++) {
        scores[i] += weights.get(i, next.getIndex());
      }
    }

    return scores;
  }

}
