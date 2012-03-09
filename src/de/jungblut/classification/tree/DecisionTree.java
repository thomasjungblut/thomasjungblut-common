package de.jungblut.classification.tree;

import de.jungblut.math.dense.DenseIntVector;

/**
 * This is the model class for a trained decision tree.
 * 
 */
public final class DecisionTree {

  // this is the global used defaul classification
  public static final int DEFAULT_CLASSIFICATION = 0;

  // column index (attribute) of this node
  private int attributeIndex = -1;
  // if this is a leaf, this is the returned prediction
  private int prediction = DEFAULT_CLASSIFICATION;
  // children of this node, could contain leafs or new nodes
  // if this is a leaf, children is null
  private DecisionTree[] children;

  public DecisionTree(int attributeIndex, int attributeCount) {
    this.attributeIndex = attributeIndex;
    this.children = new DecisionTree[attributeCount];
  }

  public DecisionTree(int prediction) {
    this.prediction = prediction;
  }

  /**
   * Add a new node as a child to this node.
   */
  public final void addNode(int attributeIndex, DecisionTree tree) {
    children[attributeIndex] = tree;
  }

  /**
   * Gets a specific child at the given attribute index.
   * 
   * @return the child.
   */
  public final DecisionTree get(int attributeIndex) {
    return children[attributeIndex];
  }

  /**
   * Turns this node to a leaf, deleting all children and setting the
   * prediction.
   */
  public final void turnToLeaf(int prediction) {
    this.children = null;
    this.prediction = prediction;
  }

  /**
   * Classfies a given DenseIntVector and returns a binary prediction.
   * 
   * @param example a given DenseIntVector to classify.
   * @return binary prediction 0 or 1.
   */
  public final int classifyInstance(final DenseIntVector example) {
    return classifyInstanceInternal(example);
  }

  private final int classifyInstanceInternal(final DenseIntVector example) {
    if (children != null) {
      final int i = example.get(attributeIndex);
      if (children.length <= i) {
        return DEFAULT_CLASSIFICATION;
      }
      final DecisionTree decisionTree = children[i];
      return decisionTree.classifyInstanceInternal(example);
    } else {
      return getPrediction();
    }
  }

  /**
   * @return true if this is a leaf
   */
  public final boolean isLeaf() {
    return children == null;
  }

  // testcase method
  final DecisionTree[] getChildren() {
    return children;
  }

  /**
   * @return a binary prediction. 0 or 1.
   */
  public final int getPrediction() {
    return prediction;
  }

  /**
   * @return the associated index to a matrix column.
   */
  public final int getAttributeIndex() {
    return attributeIndex;
  }

  @Override
  public final String toString() {
    return isLeaf() ? "Prediction: " + getPrediction() : "Columnindex: "
        + getAttributeIndex();
  }

  /*
   * Pretty print methods.
   */

  public final String toPrettyTreeString() {
    return toStringInternal(0, new StringBuilder());
  }

  private final String toStringInternal(int depth, StringBuilder buffer) {
    buffer.append(repeat("\t", depth));
    buffer.append("***");
    if (attributeIndex != -1) {
      buffer.append(attributeIndex + " \n");
    } else {
      buffer.append("\n");
    }
    if (children != null) {
      int index = 0;
      for (DecisionTree attributeValue : children) {
        if (attributeValue != null) {
          buffer.append(repeat("\t", depth + 1));
          buffer.append(index);
          buffer.append("\n");
          buffer.append(attributeValue.toStringInternal(depth + 1,
              new StringBuilder()));
        }
        index++;
      }
    } else {
      buffer.append(repeat("\t", depth + 1));
      buffer.append("=" + getPrediction());
      buffer.append("\n");
    }
    return buffer.toString();
  }

  private final String repeat(String s, int times) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < times; i++) {
      sb.append(s);
    }
    return sb.toString();
  }

}
