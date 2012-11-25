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
   * @param classes how many classes? 2 if binary.
   * @return a n x m matrix where n is the number of featurevectors and m is the
   *         number of classes (in binary prediction this is just 1, 0 and 1 are
   *         the predicted labels at index 0 then).
   */
  public static DoubleMatrix decode(DoubleMatrix weights,
      DoubleMatrix features, int classes) {
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
      int prevPosition = position - 1;
      // for each previous label
      for (int j = 0; j < classes; j++) {
        localScores = computeScores(classes,
            features.getRowVector(prevPosition), weights);
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

    DoubleMatrix outcome = new DenseDoubleMatrix(features.getRowCount(),
        classes == 2 ? 1 : classes);
    // set the probabilities into the matrix
    for (int i = 0; i < m; i++) {
      int bestLabel = 0;
      double bestScore = scores[i][bestLabel];
      for (int label = 1; label < scores[i].length; label++) {
        if (scores[i][label] > bestScore) {
          bestLabel = label;
          bestScore = scores[m - 1][label];
        }
      }
      DenseDoubleVector vec = null;
      if (classes != 2) {
        vec = new DenseDoubleVector(classes);
        vec.set(bestLabel, 1);
      } else {
        vec = new DenseDoubleVector(1);
        vec.set(0, bestLabel);
      }
      outcome.setRowVector(i, vec);
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
