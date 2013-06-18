package de.jungblut.math.minimize;

import java.util.Arrays;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Gradient descent implementation with some neat features like momentum,
 * divergence detection, delta breaks and maybe later on adaptive learning
 * rates. For more sophisticated configuration use the
 * {@link GradientDescentBuilder}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class GradientDescent extends AbstractMinimizer {

  private final double alpha;
  private final boolean breakOnDivergence;
  private final double breakDifference;
  private final double momentum;

  public static class GradientDescentBuilder {

    private final double alpha;
    private double breakDifference;
    private double momentum;
    private boolean breakOnDivergence;

    private GradientDescentBuilder(double alpha) {
      this.alpha = alpha;
    }

    public GradientDescent build() {
      return new GradientDescent(alpha, breakDifference, momentum,
          breakOnDivergence);
    }

    /**
     * Add momentum to this gradient descent minimizer.
     * 
     * @param momentum the momentum to use.
     * @return the builder again.
     */
    public GradientDescentBuilder momentum(double momentum) {
      this.momentum = momentum;
      return this;
    }

    /**
     * If called, this breaks when the gradient descent minimizer starts to
     * diverge (costs are growing).
     * 
     * @return the builder again.
     */
    public GradientDescentBuilder breakOnDivergence() {
      this.breakOnDivergence = true;
      return this;
    }

    /**
     * Breaks minimization process when the given delta in costs have been
     * archieved. Usually a quite low value of 1e-4 to 1e-8.
     * 
     * @param delta the delta to break in difference between two costs.
     * @return the builder again.
     */
    public GradientDescentBuilder breakOnDifference(double delta) {
      this.breakDifference = delta;
      return this;
    }

    /**
     * Creates a new builder.
     * 
     * @param alpha the learning rate to set.
     * @return a new builder.
     */
    public static GradientDescentBuilder create(double alpha) {
      return new GradientDescentBuilder(alpha);
    }

  }

  private GradientDescent(double alpha, double diff, double momentum,
      boolean breakOnDivergence) {
    this.alpha = alpha;
    this.breakDifference = diff;
    this.momentum = momentum;
    this.breakOnDivergence = breakOnDivergence;
  }

  /**
   * @param alpha the learning rate.
   * @param limit the delta in cost to archieve to break the iterations.
   */
  public GradientDescent(double alpha, double limit) {
    this(alpha, limit, 0d, false);
  }

  @Override
  public final DoubleVector minimize(CostFunction f, DoubleVector pInput,
      final int maxIterations, boolean verbose) {

    double[] lastCosts = new double[3];
    Arrays.fill(lastCosts, Double.MAX_VALUE);
    final int lastIndex = lastCosts.length - 1;
    DoubleVector lastTheta = null;
    DoubleVector theta = pInput;
    for (int iteration = 0; iteration < maxIterations; iteration++) {
      Tuple<Double, DoubleVector> evaluateCost = f.evaluateCost(theta);
      if (verbose) {
        System.out.print("Iteration " + iteration + " | Cost: "
            + evaluateCost.getFirst() + "\r");
      }
      shiftLeft(lastCosts);
      lastCosts[lastIndex] = evaluateCost.getFirst();
      // break if we converged below the limit
      if (converged(lastCosts, breakDifference)) {
        break;
      }
      // break if we are going in the wrong direction
      if (breakOnDivergence && ascending(lastCosts)) {
        break;
      }

      DoubleVector gradient = evaluateCost.getSecond();
      // basically subtract the gradient multiplied with the learning rate
      lastTheta = theta;
      theta = theta.subtract(gradient.multiply(alpha));
      if (lastTheta != null && momentum != 0d) {
        // we add momentum as the parameter "m" multiplied by the difference of
        // both theta vectors
        theta = theta.add((lastTheta.subtract(theta)).multiply(momentum));
      }
      onIterationFinished(iteration, evaluateCost.getFirst(), theta);
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

  static void shiftLeft(double[] lastCosts) {
    final int lastIndex = lastCosts.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      lastCosts[i] = lastCosts[i + 1];
    }
    // shift MAX_VALUE into the last position
    lastCosts[lastIndex] = Double.MAX_VALUE;
  }

  static boolean converged(double[] lastCosts, double limit) {
    return Math.abs(lastCosts[lastCosts.length - 1]
        - lastCosts[lastCosts.length - 2]) < limit;
  }

  static boolean ascending(double[] lastCosts) {
    double last = lastCosts[0];
    boolean ascending = false;
    for (int i = 1; i < lastCosts.length; i++) {
      ascending = last < lastCosts[i];
      last = lastCosts[i];
    }
    return ascending;
  }

}
