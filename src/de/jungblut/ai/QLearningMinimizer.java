package de.jungblut.ai;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.StochasticCostFunction;
import de.jungblut.math.minimize.StochasticMinimizer;
import de.jungblut.math.tuple.Tuple;

/**
 * Minimizer that works with the q-learning value update function while training
 * with a stochastic cost function.
 * 
 * @author thomas.jungblut
 * 
 */
public final class QLearningMinimizer implements StochasticMinimizer {

  private DoubleVector x;
  private DenseDoubleVector y;
  private DoubleVector theta;

  @Override
  public DoubleVector minimize(StochasticCostFunction f, DoubleVector theta,
      int maxIterations, boolean verbose) {
    this.theta = theta;
    Tuple<Double, DoubleVector> evaluateCost = f.evaluateCost(theta, x, y);
    if (verbose) {
      System.out.println("Current Cost: " + evaluateCost.getFirst());
    }
    return theta;
  }

  public void update(DoubleVector networkOutput, double reward,
      double learningRate, double discount) {
    theta = theta.add(learningRate * (reward + discount * networkOutput.max()));
  }

  public DoubleVector getTheta() {
    return this.theta;
  }

  public void setFeatures(DoubleVector x) {
    this.x = x;
  }

  public void setOutcome(DenseDoubleVector y) {
    this.y = y;
  }

}
