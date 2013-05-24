package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Newton-Raphson method for minimizing functions. Geometrically it intersects
 * the x-axis with the tangent to f at f(x) until it converges to a point where
 * the gradient is near to zero.
 * 
 * @author thomas.jungblut
 * 
 */
public final class NewtonRaphson extends AbstractMinimizer {

  @Override
  public DoubleVector minimize(CostFunction f, DoubleVector theta,
      int maxIterations, boolean verbose) {

    double lastCost = Double.MAX_VALUE;
    for (int i = 0; i < maxIterations; i++) {
      Tuple<Double, DoubleVector> evaluateCost = f.evaluateCost(theta);
      double cost = evaluateCost.getFirst().doubleValue();
      DoubleVector gradient = evaluateCost.getSecond();
      if (verbose) {
        System.out.println("Iteration: " + i + " | Cost: " + cost);
      }
      if (lastCost > cost) {
        lastCost = cost;
      } else {
        // break if we start diverging
        break;
      }
      theta = theta.subtract(gradient.divideFrom(cost));
    }

    return theta;
  }

  /**
   * Minimizes the given cost function with the newton-raphson method.
   * 
   * @param f the costfunction to minimize.
   * @param pInput the start parameters.
   * @param length the number of maximum iterations to make.
   * @param verbose if true iteration progress will be printed to STDOUT.
   * @return the optimized parameterset theta.
   */
  public static DoubleVector minimizeFunction(CostFunction f,
      DoubleVector pInput, int length, final boolean verbose) {
    return new NewtonRaphson().minimize(f, pInput, length, verbose);
  }

}
