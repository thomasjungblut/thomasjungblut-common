package de.jungblut.math.minimize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class FmincgTest {

  @Test
  public void testSimpleParable() {
    int startPoint = -5;
    // start at x=-5
    DoubleVector start = new DenseDoubleVector(new double[] { startPoint });

    // our function is f(x) = (4-x)^2+10
    // the derivative is f'(x) = 2x-8
    CostFunction inlineFunction = new CostFunction() {
      @Override
      public CostGradientTuple evaluateCost(DoubleVector input) {

        double cost = Math.pow(4 - input.get(0), 2) + 10;
        DenseDoubleVector gradient = new DenseDoubleVector(
            new double[] { 2 * input.get(0) - 8 });

        return new CostGradientTuple(cost, gradient);
      }
    };

    DoubleVector minimizeFunction = Fmincg.minimizeFunction(inlineFunction,
        start, 100, false);
    assertEquals(4.0d, minimizeFunction.get(0), 1e-5);
  }

}
