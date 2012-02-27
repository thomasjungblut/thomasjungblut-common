package de.jungblut.classification.tree;

import de.jungblut.math.DenseIntVector;
import de.jungblut.math.DenseIntMatrix;

/**
 * 
 * @author thanks to Marvin Ritter who is better in coding math equations than
 *         me :)
 * 
 */
public final class EntropyCalculator {

  static final double LOG_TWO = Math.log(2);

  /**
   * Returns the index of a feature which has the highest information gain using
   * entropy. If multiple attributes have the same gain the first one will be
   * chosen.
   * 
   * @return Index of the column with the highest information gain or -1 if all
   *         not target columns are already used
   * @throws MatrixFormatException
   */
  public final int getColumnWithHighestGain(DenseIntMatrix matrix,
      DenseIntVector outputVariable, boolean[] columnsAlreadyUsed) {
    /*
     * Choose the column/attribute with the highest information gain. Gain(S, A)
     * = Entropy(S) - \sum_{v \in Values(A)} \frac{|S_v|}{|S|} * Entropy(S_v)
     * The part in the sum is the weighted entropy. For choosing the best
     * column/attribute the Entropy(S) can be ignored because it is the constant
     * for all columns.
     */

    double minWeightedEntropySum = Double.MAX_VALUE;
    int column = -1;

    for (int c = 0; c < matrix.getColumnCount(); c++) {
      if (!columnsAlreadyUsed[c]) {
        double x = getWeightedEntropySum(matrix, outputVariable, c);
        if (x < minWeightedEntropySum) {
          minWeightedEntropySum = x;
          column = c;
        }
      }
    }

    return column;
  }

  public final double getWeightedEntropySum(DenseIntMatrix matrix,
      DenseIntVector outputVariable, int col) {

    final int[][] m = matrix.getValues();
    final int columnValues = new DenseIntVector(matrix.getColumn(col))
        .getMaxValue();
    final int targetValues = outputVariable.getNumberOfDistinctElements();

    // count predictions for each possible value in this column
    int[] valueCounts = new int[columnValues + 1]; /*
                                                    * |S_v| for each attribute
                                                    */
    int[][] predictionCounts = new int[columnValues + 1][targetValues + 1];
    for (int r = 0; r < m.length; r++) {
      int value = m[r][col];
      int prediction = outputVariable.get(r);
      valueCounts[value]++;
      predictionCounts[value][prediction]++;
    }

    double sum = 0.0;
    double totalRows = (double) m.length;
    for (int v = 0; v < valueCounts.length; v++) {
      if (valueCounts[v] > 0) {
        sum += valueCounts[v] / totalRows
            * getEntropy(predictionCounts[v], valueCounts[v]);
      }
    }

    return sum;
  }

  private final double getEntropy(int[] predictionCounts, int sum) {
    double entropy = 0.0;
    for (int prediction : predictionCounts) {
      if (prediction != 0 && prediction != sum) {
        double p = (double) prediction / sum;
        entropy -= p * Math.log(p) / LOG_TWO;
      }
    }

    return entropy;
  }

}
