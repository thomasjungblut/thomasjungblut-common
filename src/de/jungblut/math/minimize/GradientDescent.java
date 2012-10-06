package de.jungblut.math.minimize;

import java.util.Arrays;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Simple gradient descent.
 * 
 * @author thomas.jungblut
 * 
 */
public final class GradientDescent implements Minimizer {

  private final double alpha;
  private final double limit;

  /**
   * @param alpha the learning rate.
   * @param limit the cost to archieve to break the iterations.
   */
  public GradientDescent(double alpha, double limit) {
    this.alpha = alpha;
    this.limit = limit;
  }

  @Override
  public final DoubleVector minimize(CostFunction f, DoubleVector pInput,
      final int maxIterations, boolean verbose) {

    double[] lastCosts = new double[3];
    Arrays.fill(lastCosts, Double.MAX_VALUE);
    final int lastIndex = lastCosts.length - 1;
    DoubleVector theta = pInput;
    for (int iteration = 0; iteration < maxIterations; iteration++) {
      Tuple<Double, DoubleVector> evaluateCost = f.evaluateCost(theta);
      if (verbose) {
        System.out.print("Interation " + iteration + " | Cost: "
            + evaluateCost.getFirst() + "\r");
      }
      shiftLeft(lastCosts);
      lastCosts[lastIndex] = evaluateCost.getFirst();
      // break if we converged below the limit or have degraded into gradient
      // ascent due to too large learning rate
      if (converged(lastCosts, limit) || ascending(lastCosts)) {
        break;
      }
      DoubleVector gradient = evaluateCost.getSecond();
      // basically subtract the gradient multiplied with the learning rate
      theta = theta.subtract(gradient.multiply(alpha));
    }

    return theta;

  }

  /**
   * Minimize a given cost function f with the initial parameters pInput (also
   * called theta) with a learning rate alpha and a fixed number of iterations.
   * The loop can break earlier if costs converge below the limit. If the same
   * cost was archieved three times in a row, it will also break the iterations.
   * 
   * @param f the function to minimize.
   * @param pInput the starting parameters.
   * @param alpha the learning rate.
   * @param limit the cost to archieve to break the iterations.
   * @param length the number of iterations.
   * @param verbose if true prints progress to STDOUT.
   * @return the learned minimal parameters.
   */
  public static DoubleVector minimizeFunction(CostFunction f,
      DoubleVector pInput, double alpha, double limit, int length,
      final boolean verbose) {
    return new GradientDescent(alpha, limit).minimize(f, pInput, length,
        verbose);
  }

  private static void shiftLeft(double[] lastCosts) {
    final int lastIndex = lastCosts.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      lastCosts[i] = lastCosts[i + 1];
    }
    // shift MAX_VALUE into the last position
    lastCosts[lastIndex] = Double.MAX_VALUE;
  }

  private static boolean converged(double[] lastCosts, double limit) {
    return Math.abs(lastCosts[lastCosts.length - 1]
        - lastCosts[lastCosts.length - 2]) < limit;
  }

  private static boolean ascending(double[] lastCosts) {
    double last = lastCosts[0];
    boolean ascending = false;
    for (int i = 1; i < lastCosts.length; i++) {
      ascending = last < lastCosts[i];
      last = lastCosts[i];
    }
    return ascending;
  }

}
