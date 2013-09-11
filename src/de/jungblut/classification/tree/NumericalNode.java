package de.jungblut.classification.tree;

import de.jungblut.math.DoubleVector;

public class NumericalNode extends AbstractTreeNode {

  int splitAttributeIndex;
  int splitAttributeValue;

  AbstractTreeNode lower;
  AbstractTreeNode higher;

  @Override
  public int predict(DoubleVector features) {
    if (features.get(splitAttributeIndex) > splitAttributeValue) {
      return higher.predict(features);
    } else {
      return lower.predict(features);
    }
  }
}
