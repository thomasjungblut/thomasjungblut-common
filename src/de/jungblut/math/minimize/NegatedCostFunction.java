package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;

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
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {
    Tuple<Double, DoubleVector> evaluateCost = minableCostFunction
        .evaluateCost(input);
    return new Tuple<>(-evaluateCost.getFirst(),
        evaluateCost.getSecond().multiply(-1));
  }

}
