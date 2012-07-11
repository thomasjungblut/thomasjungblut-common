package de.jungblut.math.minimize;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public class GradientDescentTest extends TestCase {

  @Test
  public void testGradientDescent() {

    DoubleVector start = new DenseDoubleVector(new double[] { 5, 3 });

    // our function is f(x,y) = x^2+y^2
    // the derivative is f'(x,y) = 2x+2y
    CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

        double cost = cost(input);
        DenseDoubleVector gradient = new DenseDoubleVector(new double[] {
            2 * input.get(0), 2 * input.get(1) });

        return new Tuple<Double, DoubleVector>(cost, gradient);
      }
    };

    DoubleVector minimizeFunction = GradientDescent.minimizeFunction(
        inlineFunction, start, 0.1d, 1E-10, 1000, false);
    // 1E-5 is close enough to zero for the test to pass
    assertTrue(minimizeFunction.get(0) > 0 && minimizeFunction.get(0) < 1E-5);
    assertTrue(minimizeFunction.get(1) > 0 && minimizeFunction.get(1) < 1E-5);
  }

  public double cost(DoubleVector input) {
    return Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);
  }
}
