package de.jungblut.math.minimize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class NegatedCostFunctionTest {

  @Test
  public void testMaximize() {
    // maximize -x^2-y^2
    // derivative is -2*x and -2*y
    // the max should be at 0
    DoubleVector theta = new DenseDoubleVector(new double[] { -25, -25 });
    DoubleVector minimizeFunction = Fmincg.minimizeFunction(
        new NegatedCostFunction(new CostFunction() {
          @Override
          public CostGradientTuple evaluateCost(DoubleVector input) {
            double cost = -Math.pow(input.get(0), 2)
                - Math.pow(input.get(1), 2);
            DenseDoubleVector gradient = new DenseDoubleVector(new double[] {
                -2 * input.get(0), -2 * input.get(1) });

            return new CostGradientTuple(cost, gradient);
          }
        }), theta, 10, false);

    assertEquals(0d, minimizeFunction.get(0), 1e-5);
    assertEquals(0d, minimizeFunction.get(1), 1e-5);
  }

}
