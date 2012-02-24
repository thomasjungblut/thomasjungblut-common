package de.jungblut.classification.tree;

import de.jungblut.math.DenseIntVector;
import de.jungblut.math.Matrix;

public final class EntropyCalculator {

  static final double LOG_BASE_TWO = Math.log(2);

  /**
   * Returns the index of a feature which has the lowest entropy and wasn't used
   * before. This method can be used for external tracking of what attributes
   * have been returned to filter.
   * 
   * @return -1 if all features has been examined from outside. >=0 index of
   *         what attribute in feature should be used next. (based on min
   *         entropy).
   */
  public final int getIndexWithMinEntropy(Matrix inputFeatures,
      DenseIntVector outputVariables, boolean[] alreadyUsedIndicesList) {
    
    final int columns = inputFeatures.getColumnCount();
    int entropyIndex = -1;
    double lowestEntropy = Double.MAX_VALUE;
    for (int i = 0; i < columns; i++) {
      if (!alreadyUsedIndicesList[i]) {
        double weightedEntropySum = getWeightedEntropySum(new DenseIntVector(
            inputFeatures.getColumn(i)), outputVariables);
        if (weightedEntropySum < lowestEntropy && weightedEntropySum > 0) {
          lowestEntropy = weightedEntropySum;
          entropyIndex = i;
        }
      }
    }
    if (entropyIndex >= 0) {
      alreadyUsedIndicesList[entropyIndex] = true;
    }
    return entropyIndex;
  }

  public final double getWeightedEntropySum(DenseIntVector attributeColumn,
      DenseIntVector outputVariables) {
    final int length = attributeColumn.getLength();
    final int distinctAttributes = attributeColumn
        .getNumberOfDistinctElements();
    final int[] attributeCount = new int[distinctAttributes];
    /*
     * This is always two dimensional, on the first dimension is the attribute,
     * in the second dimension are the counts for the predition.
     */
    final int[][] preditionCount = new int[distinctAttributes][2];

    for (int i = 0; i < length; i++) {
      final int attributeIndex = attributeColumn.get(i);
      final int prediction = outputVariables.get(i);
      // This "hack" is only working if our DenseIntVector elements strictly
      // start with zero and are incremented
      // afterwards.
      attributeCount[attributeIndex]++;
      preditionCount[attributeIndex][prediction]++;
    }

    double weightedEntropySum = 0.0;
    // log base two is calculated as following:
    // (Math.log(x) / Math.log(2)
    for (int i = 0; i < distinctAttributes; i++) {
      if (preditionCount[i][0] > 0 && preditionCount[i][1] > 0) {
        // only working for binary classification
        // DANGER! Sophisticated math function incoming!
        // weightedEntropySum += ((double) attributeCount[i] / (double) length)
        // * (((-(double) preditionCount[i][0] / (double) attributeCount[i]) *
        // (Math
        // .log((double) preditionCount[i][0] / (double) attributeCount[i]) /
        // LOG_BASE_TWO)) - ((-(double) preditionCount[i][1] / (double)
        // attributeCount[i]) * (Math
        // .log((double) preditionCount[i][1] / (double) attributeCount[i]) /
        // LOG_BASE_TWO)));
        // TODO this is wrong because of a wrong minus
      }
    }
    // we have to absolute our calculation because negative values make no sense
    // in probability.
    return Math.abs(weightedEntropySum);
  }
}
