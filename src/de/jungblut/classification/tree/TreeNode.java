package de.jungblut.classification.tree;

import de.jungblut.math.DoubleVector;

public interface TreeNode {

  /**
   * @return predicts the index of the outcome, or -1 if not known. In the
   *         binary case, 0 and 1 are used to distinguish.
   */
  public int predict(DoubleVector features);

}
