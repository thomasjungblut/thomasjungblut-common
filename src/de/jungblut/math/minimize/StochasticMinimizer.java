package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;

/**
 * Minimizer interface for various function minimizers.
 * 
 * @author thomas.jungblut
 * 
 */
public interface StochasticMinimizer {

  /**
   * Minimizes the given costfunction with the starting parameter theta.
   * 
   * @param f the costfunction to minimize.
   * @param theta the starting parameters.
   * @param maxIterations the number of iterations to do.
   * @param verbose if TRUE it will print progress.
   * @return the optimized theta parameters.
   */
  public DoubleVector minimize(StochasticCostFunction f, DoubleVector theta,
      int maxIterations, boolean verbose);

}
