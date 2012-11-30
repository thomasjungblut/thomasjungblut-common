package de.jungblut.math.minimize;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public class NegatedCostFunctionTest extends TestCase {

  @Test
  public void testMaximize() {
    // maximize -x^2-y^2
    // derivative is -2*x and -2*y
    // the max should be at 0
    DoubleVector theta = new DenseDoubleVector(new double[] { -25, -25 });
    DoubleVector minimizeFunction = Fmincg.minimizeFunction(
        new NegatedCostFunction(new CostFunction() {
          @Override
          public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {
            double cost = -Math.pow(input.get(0), 2)
                - Math.pow(input.get(1), 2);
            DenseDoubleVector gradient = new DenseDoubleVector(new double[] {
                -2 * input.get(0), -2 * input.get(1) });

            return new Tuple<Double, DoubleVector>(cost, gradient);
          }
        }), theta, 10, false);

    assertEquals(0d, minimizeFunction.get(0));
    assertEquals(0d, minimizeFunction.get(1));
  }

}
