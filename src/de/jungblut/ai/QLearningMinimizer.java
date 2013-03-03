package de.jungblut.ai;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.StochasticCostFunction;
import de.jungblut.math.minimize.StochasticMinimizer;

/**
 * Minimizer that works with the q-learning value update function while training
 * with a stochastic cost function.
 * 
 * @author thomas.jungblut
 * 
 */
public class QLearningMinimizer implements StochasticMinimizer {

  @Override
  public DoubleVector minimize(StochasticCostFunction f, DoubleVector theta,
      int maxIterations, boolean verbose) {
    // TODO
    /*
     * for each state we have, run the cost function and check for rewards- then
     * apply the gradients to our theta vector.
     */

    return theta;
  }

}
