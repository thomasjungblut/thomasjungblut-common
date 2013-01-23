package de.jungblut.math.minimize;

import static de.jungblut.math.minimize.GradientDescent.ascending;
import static de.jungblut.math.minimize.GradientDescent.converged;
import static de.jungblut.math.minimize.GradientDescent.shiftLeft;

import java.util.Arrays;

import de.jungblut.datastructure.InputProvider;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Simple stochastic gradient descent.
 * 
 * @author thomas.jungblut
 * 
 */
public final class StochasticGradientDescent implements StochasticMinimizer {

  private final double alpha;
  private final double limit;
  private final InputProvider<Tuple<DoubleVector, DenseDoubleVector>> provider;

  /**
   * @param provider the input provider to get the data from.
   * @param alpha the learning rate.
   * @param limit the cost to archieve to break the iterations.
   */
  public StochasticGradientDescent(
      InputProvider<Tuple<DoubleVector, DenseDoubleVector>> provider,
      double alpha, double limit) {
    this.provider = provider;
    this.alpha = alpha;
    this.limit = limit;
  }

  @Override
  public final DoubleVector minimize(StochasticCostFunction f,
      DoubleVector pInput, final int maxIterations, boolean verbose) {

    double[] lastCosts = new double[3];
    Arrays.fill(lastCosts, Double.MAX_VALUE);
    final int lastIndex = lastCosts.length - 1;
    DoubleVector theta = pInput;
    for (int iteration = 0; iteration < maxIterations; iteration++) {
      int n = 0;
      double costSum = 0d;
      Iterable<Tuple<DoubleVector, DenseDoubleVector>> iterable = provider
          .iterate();
      // basically iterate over all the stuff in each iteration and make direct
      // updates
      for (Tuple<DoubleVector, DenseDoubleVector> data : iterable) {
        Tuple<Double, DoubleVector> evaluateCost = f.evaluateCost(theta,
            data.getFirst(), data.getSecond());
        costSum += evaluateCost.getFirst();
        theta = theta.subtract(evaluateCost.getSecond().multiply(alpha));
        n++;
      }
      double cost = costSum / n;
      if (verbose) {
        System.out.print("Iteration " + iteration + " | Cost: " + cost + "\r");
      }
      shiftLeft(lastCosts);
      lastCosts[lastIndex] = cost;
      // break if we converged below the limit or have degraded into gradient
      // ascent due to too large learning rate
      if (converged(lastCosts, limit) || ascending(lastCosts)) {
        break;
      }
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
   * @param provider the input provider for the costfunction to minimize.
   * @param pInput the starting parameters.
   * @param alpha the learning rate.
   * @param limit the cost to archieve to break the iterations.
   * @param length the number of iterations.
   * @param verbose if true prints progress to STDOUT.
   * @return the learned minimal parameters.
   */
  public static DoubleVector minimizeFunction(StochasticCostFunction f,
      InputProvider<Tuple<DoubleVector, DenseDoubleVector>> provider,
      DoubleVector pInput, double alpha, double limit, int length,
      final boolean verbose) {
    return new StochasticGradientDescent(provider, alpha, limit).minimize(f,
        pInput, length, verbose);
  }

}
