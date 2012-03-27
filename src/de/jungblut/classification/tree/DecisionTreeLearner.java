package de.jungblut.classification.tree;

import java.util.ArrayList;

import de.jungblut.math.dense.DenseIntMatrix;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Decision tree for nominal attributes and binary classification. <br/>
 * TODO extend to nominal classification.
 * 
 * @author thomas.jungblut
 * 
 */
public final class DecisionTreeLearner {

  private EntropyCalculator attributeChooser = new EntropyCalculator();
  private DenseIntMatrix algorithmInputFeatures;

  /**
   * For ml-class.org mates inputFeatures is equal to "X" whereas outputVariable
   * is equal to "y" in octave excersises.
   * 
   * @return a trained DecisionTree model.
   */
  public final DecisionTree train(DenseIntMatrix inputFeatures,
      DenseIntVector outputVariable) {
    this.algorithmInputFeatures = inputFeatures;
    return trainInternal(inputFeatures, outputVariable,
        new boolean[inputFeatures.getColumnCount()]);
  }

  /**
   * Recursive TDIDT training of decision trees.
   * 
   * @return a trained model which is able to classify datasets.
   */
  private final DecisionTree trainInternal(DenseIntMatrix inputFeatures,
      DenseIntVector outputVariable, boolean[] filteredAttributes) {
    // return default prediction if we have no data
    if (inputFeatures.getRowCount() == 0) {
      return new DecisionTree(DecisionTree.DEFAULT_CLASSIFICATION);
    }
    // if we just have a single attribute value left in our prediction, we
    // return this
    if (outputVariable.getNumberOfDistinctElements() == 1) {
      return new DecisionTree(outputVariable.get(0));
    }
    // we look which attribute has the lowest entropy
    final int bestAttributeIndex = attributeChooser.getColumnWithHighestGain(
        inputFeatures, outputVariable, filteredAttributes);
    if (bestAttributeIndex != -1) {
      // look how many attribute values exist
      final int[] attributeValues = algorithmInputFeatures
          .getDistinctElementsAsArray(bestAttributeIndex);

      DecisionTree node = new DecisionTree(bestAttributeIndex,
          attributeValues.length);
      for (int i = 0; i < attributeValues.length; i++) {
        // we are filtering the matrix and output according to our
        // attributes
        final Tuple<DenseIntMatrix, DenseIntVector> filterAttribute = filterAttribute(
            inputFeatures, outputVariable, bestAttributeIndex, i);
        // make a recursive step with the new attribute
        final DecisionTree trainInternal = trainInternal(
            filterAttribute.getFirst(), filterAttribute.getSecond(),
            cloneArray(filteredAttributes));
        // make sure that this method never returns null, otherwise this
        // will
        // fail greatly.
        node.addNode(attributeValues[i], trainInternal);
      }

      node = mergeSameChildren(attributeValues, node);

      // our node is trained completly, now return
      return node;
    } else {
      // if our entropy isn't reliable enough, use majority value of
      // predictions
      return new DecisionTree(getMajorityPrediction(outputVariable));
    }
  }

  /**
   * This method merges children of a given node and it's attributes if they
   * have the same prediction.
   * 
   * @return the same node or a node that has been turned to a leaf.
   */
  private final DecisionTree mergeSameChildren(final int[] attributeValues,
      final DecisionTree node) {
    final int[] predictionChildCount = new int[2];
    // merge children with the same prediction
    for (int i = 0; i < attributeValues.length; i++) {
      final DecisionTree child = node.get(i);
      if (child != null && child.isLeaf()) {
        predictionChildCount[child.getPrediction()]++;
      } else {
        // if we find something that is no leaf, we have to return
        // directly
        return node;
      }
    }
    if ((predictionChildCount[0] != 0 && predictionChildCount[1] == 0)
        || predictionChildCount[1] != 0 && predictionChildCount[0] == 0) {
      node.turnToLeaf(predictionChildCount[0] > predictionChildCount[1] ? 0 : 1);
    }
    return node;
  }

  /**
   * Helper method to determine how many predictions are either zero or one.
   * 
   * @return either 0 or 1, based on majority of counts.
   */
  private final int getMajorityPrediction(DenseIntVector v) {
    final int[] counts = new int[2];
    for (int i = 0; i < v.getLength(); i++) {
      counts[v.get(i)]++;
    }
    return counts[0] > counts[1] ? 0 : 1;
  }

  /**
   * Filters the given matrix and DenseIntVector based on given column where the
   * value is equal to attributeValue.
   * 
   * @param inputFeatures matrix to filter on
   * @param outputVariable ouput DenseIntVector to filter on
   * @param attributeIndex which column of the matrix to filter
   * @param attributeValue which value of column to filter
   * @return a new tuple of matrix and DenseIntVector where the rows with the
   *         given attributeIndex and attributeValue are not included.
   */
  private final Tuple<DenseIntMatrix, DenseIntVector> filterAttribute(
      DenseIntMatrix inputFeatures, DenseIntVector outputVariable,
      int attributeIndex, int attributeValue) {
    final ArrayList<int[]> rowList = new ArrayList<int[]>();
    final ArrayList<Integer> outList = new ArrayList<Integer>();
    final int rowCount = inputFeatures.getRowCount();
    for (int i = 0; i < rowCount; i++) {
      if (inputFeatures.get(i, attributeIndex) == attributeValue) {
        rowList.add(inputFeatures.getRow(i));
        outList.add(outputVariable.get(i));
      }
    }

    final int[] newOutput = new int[outList.size()];
    for (int i = 0; i < outList.size(); i++) {
      newOutput[i] = outList.get(i);
    }

    return new Tuple<DenseIntMatrix, DenseIntVector>(new DenseIntMatrix(
        rowList.toArray(new int[0][0])), new DenseIntVector(newOutput));

  }

  /**
   * Clone method for boolean arrays. Uses fast System.arraycopy which is
   * internal implemented by ASM code.
   * 
   * @return a brand new array of given given one.
   */
  private final boolean[] cloneArray(boolean[] arr) {
    final boolean[] toReturn = new boolean[arr.length];
    System.arraycopy(arr, 0, toReturn, 0, arr.length);
    return toReturn;
  }

  public static void main(String[] args) {
    // sample vectorized set where the last column is the prediction
    int[][] intMatrix = new int[][] { { 0, 0, 1, 1, 2, 1 },
        { 0, 0, 1, 1, 0, 1 }, { 1, 0, 0, 2, 1, 0 }, { 0, 1, 1, 0, 2, 0 },
        { 1, 0, 0, 0, 0, 1 }, { 1, 1, 1, 2, 2, 1 }, { 1, 1, 1, 2, 1, 0 },
        { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 1, 1 }, { 0, 1, 0, 1, 2, 1 },
        { 1, 1, 1, 1, 0, 0 } };
    DenseIntMatrix matrix = new DenseIntMatrix(intMatrix);
    Tuple<DenseIntMatrix, DenseIntVector> tuple = matrix.splitLastColumn();

    DecisionTreeLearner learner = new DecisionTreeLearner();
    DecisionTree train = learner.train(tuple.getFirst(), tuple.getSecond());

    System.out.println(train.toPrettyTreeString());

  }
}
