package de.jungblut.classification.tree;

import de.jungblut.math.DoubleVector;

public final class LeafNode implements TreeNode {

  private final int label;

  public LeafNode(int label) {
    this.label = label;
  }

  @Override
  public int predict(DoubleVector features) {
    return label;
  }

}
