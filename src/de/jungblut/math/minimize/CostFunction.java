package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;

public interface CostFunction {

  /**
   * Evaluation for the cost function to retrieve cost and gradient.
   * 
   * @param input a given input vector
   * @return a tuple consists of J (cost) and a vector X which is the gradient
   *         of the input.
   */
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input);

}
