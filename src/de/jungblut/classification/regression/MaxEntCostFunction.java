package de.jungblut.classification.regression;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.math.tuple.Tuple;

public final class MaxEntCostFunction implements CostFunction {

  // the probability that a ngram occures in context of class x, denoted by
  // indices, the columns are the classes
  private SparseDoubleColumnMatrix probabilities;

  public MaxEntCostFunction() {
    
  }

  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

    double j = 0; // shannons entropy

    DoubleVector gradient = null;

    return new Tuple<>(j, gradient);
  }

  static DoubleVector logVector(DoubleVector input) {
    DoubleVector vx = new DenseDoubleVector(input.getLength());

    for (int i = 0; i < input.getLength(); i++) {
      vx.set(i, Math.log(input.get(i)));
    }

    return vx;
  }

}
