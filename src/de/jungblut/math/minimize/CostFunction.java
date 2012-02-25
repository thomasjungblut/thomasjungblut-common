package de.jungblut.math.minimize;

import de.jungblut.math.DenseDoubleVector;
import de.jungblut.util.Tuple;

public interface CostFunction {

  /**
   * Evaluation for the cost function to retrieve cost and gradient.
   * 
   * @param input a given input vector
   * @return a tuple consists of J (cost) and a vector X which is the gradient
   *         of the input.
   */
  public Tuple<Double, DenseDoubleVector> evaluateCost(DenseDoubleVector input);

}
