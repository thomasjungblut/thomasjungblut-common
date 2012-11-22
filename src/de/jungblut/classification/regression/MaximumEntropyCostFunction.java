package de.jungblut.classification.regression;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.math.minimize.NegatedCostFunction;
import de.jungblut.math.tuple.Tuple;

/**
 * MaxEnt regression cost function to optimize with an arbitrary
 * {@link Minimizer} in conjunction with a {@link NegatedCostFunction}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MaximumEntropyCostFunction implements CostFunction {

  private final DoubleMatrix x;
  private final DoubleVector y;
  private final int m;

  public MaximumEntropyCostFunction(DoubleMatrix x, DoubleVector y) {
    // add a column of ones to handle the intercept term
    this.x = new DenseDoubleMatrix(DenseDoubleVector.ones(y.getLength()), x);
    this.y = y;
    this.m = y.getLength();
  }

  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

    double j = 0d;
    DoubleVector gradient = input;

    return new Tuple<>(j, gradient);
  }

}
