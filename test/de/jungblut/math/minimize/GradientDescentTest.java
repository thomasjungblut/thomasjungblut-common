package de.jungblut.math.minimize;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.math.DoubleMath;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public class GradientDescentTest extends TestCase {

  @Test
  public void testGradientDescent() {

    DoubleVector start = new DenseDoubleVector(new double[] { 2, -1 });

    // our function is f(x,y) = x^2+y^2
    // the derivative is f'(x,y) = 2x+2y
    CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

        double cost = Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);
        DenseDoubleVector gradient = new DenseDoubleVector(new double[] {
            input.get(0) * 2, input.get(1) * 2 });

        return new Tuple<Double, DoubleVector>(cost, gradient);
      }
    };

    DoubleVector minimizeFunction = GradientDescent.minimizeFunction(
        inlineFunction, start, 0.5d, 1E-20, 1000, false);
    // 1E-5 is close enough to zero for the test to pass
    assertEquals(0, DoubleMath.fuzzyCompare(minimizeFunction.get(0), 0, 1E-5));
    assertEquals(0, DoubleMath.fuzzyCompare(minimizeFunction.get(1), 0, 1E-5));
  }

}
