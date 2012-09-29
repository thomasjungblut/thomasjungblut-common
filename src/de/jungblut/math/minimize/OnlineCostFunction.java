package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Online cost function interface to help a online miminizer to get a good
 * parameter set / minimum.
 */
public interface OnlineCostFunction {

  /**
   * Evaluate the cost and gradient at the parameters theta with the given
   * online input.
   * 
   * @param theta the current parameter estimation.
   * @param input the current online input.
   * @return a tuple that contains cost in the first and the gradient on the
   *         second dimension.
   */
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector theta,
      DoubleVector input);

}
