package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;

/**
 * Negated cost function to implement maximization problems. It simply negates
 * the gradient input and the cost, so this change is transparent to the
 * minimizer that is calling this function.
 * 
 * @author thomas.jungblut
 * 
 */
public final class NegatedCostFunction implements CostFunction {

  private CostFunction minableCostFunction;

  public NegatedCostFunction(CostFunction minableCostFunction) {
    this.minableCostFunction = minableCostFunction;
  }

  @Override
  public CostGradientTuple evaluateCost(DoubleVector input) {
    CostGradientTuple evaluateCost = minableCostFunction.evaluateCost(input);
    return new CostGradientTuple(-evaluateCost.getCost(), evaluateCost
        .getGradient().multiply(-1));
  }

}
