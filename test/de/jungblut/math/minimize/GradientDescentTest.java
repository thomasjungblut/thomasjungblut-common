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
    // the derivative is f'(x,y) = (f(x,y)-2x)/(2y-f(x,y))
    CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

        double cost = cost(input);
        DenseDoubleVector gradient = new DenseDoubleVector(new double[] {
            cost(input) - 2 * input.get(0), 2 * input.get(1) - cost(input) });

        return new Tuple<Double, DoubleVector>(cost, gradient);
      }
    };

    DoubleVector minimizeFunction = GradientDescent.minimizeFunction(
        inlineFunction, start, 0.001d, 1E-10, 100000, true);
    System.out.println(minimizeFunction);
    // assertEquals(4.0d, minimizeFunction.get(0));

  }

  public double cost(DoubleVector input) {
    return Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);
  }
}
