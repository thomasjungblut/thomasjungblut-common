package de.jungblut.classification.tree;

import de.jungblut.math.DoubleVector;

public class LeafNode extends AbstractTreeNode {

  private int label;

  public LeafNode(int label) {
    this.label = label;
  }

  @Override
  public int predict(DoubleVector features) {
    return label;
  }

}
