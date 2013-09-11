package de.jungblut.classification.tree;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;

public class NominalNode extends AbstractTreeNode {

  int splitAttributeIndex;
  // this is a parallel array to the children
  int[] nominalSplitValues;
  AbstractTreeNode[] children;

  @Override
  public int predict(DoubleVector features) {
    int classIndex = (int) features.get(splitAttributeIndex);
    int foundIndex = ArrayUtils.find(nominalSplitValues, classIndex);
    if (foundIndex != -1) {
      return children[foundIndex].predict(features);
    }
    return foundIndex;
  }

}
