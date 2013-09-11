package de.jungblut.classification.tree;

import de.jungblut.math.DoubleVector;

public final class NumericalNode implements TreeNode {

  private final int splitAttributeIndex;
  private final double splitAttributeValue;

  private final TreeNode lower;
  private final TreeNode higher;

  public NumericalNode(int splitAttributeIndex, double splitAttributeValue,
      TreeNode lower, TreeNode higher) {
    super();
    this.splitAttributeIndex = splitAttributeIndex;
    this.splitAttributeValue = splitAttributeValue;
    this.lower = lower;
    this.higher = higher;
  }

  @Override
  public int predict(DoubleVector features) {
    if (features.get(splitAttributeIndex) > splitAttributeValue) {
      return higher.predict(features);
    } else {
      return lower.predict(features);
    }
  }
}
