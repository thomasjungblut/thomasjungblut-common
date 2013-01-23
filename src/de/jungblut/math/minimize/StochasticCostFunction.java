package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Cost function interface to be implemented when using with a stochastic
 * minimizer. Stochastic in this regard means that we are updating the weights
 * after seeing a single example ("online") instead of doing a complete batch
 * like the normal {@link CostFunction} does.
 * 
 * @author thomas.jungblut
 * 
 */
public interface StochasticCostFunction {

  /**
   * Evaluation for the cost function to retrieve cost and gradient.
   * 
   * @param input a given input vector (the trained and optimized weights).
   * @param x the training example.
   * @param y the corresponding outcome vector to x.
   * @return a tuple consists of J (cost) and a vector X which is the gradient
   *         of the input.
   */
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input,
      DoubleVector x, DenseDoubleVector y);

}
