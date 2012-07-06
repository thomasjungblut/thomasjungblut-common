package de.jungblut.classification.nn;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.tuple.Tuple;

public class MultilayerPerceptronCostFunction implements CostFunction {

  @SuppressWarnings("unused")
  private final MultilayerPerceptron network;

  public MultilayerPerceptronCostFunction(MultilayerPerceptron network) {
    this.network = network;
  }

  /**
   * Input contains the network parameters (weights) as a folded vector.
   */
  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

    return null;
  }

}
