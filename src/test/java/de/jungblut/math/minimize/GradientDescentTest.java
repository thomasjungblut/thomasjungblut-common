package de.jungblut.math.minimize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.GradientDescent.GradientDescentBuilder;

public class GradientDescentTest {

  @Test
  public void testGradientDescent() {

    DoubleVector start = new DenseDoubleVector(new double[] { 2, -1 });

    CostFunction inlineFunction = getCostFunction();

    DoubleVector minimizeFunction = GradientDescent.minimizeFunction(
        inlineFunction, start, 0.5d, 1E-20, 1000, false);
    // 1E-5 is close enough to zero for the test to pass
    assertEquals(minimizeFunction.get(0), 0, 1E-5);
    assertEquals(minimizeFunction.get(1), 0, 1E-5);
  }

  @Test
  public void testBoldDriver() {
    DoubleVector start = new DenseDoubleVector(new double[] { 2, -1 });

    CostFunction inlineFunction = getCostFunction();
    GradientDescent gd = GradientDescentBuilder.create(0.8d)
        .breakOnDifference(1e-20).boldDriver().build();
    DoubleVector minimizeFunction = gd.minimize(inlineFunction, start, 1000,
        false);
    // 1E-5 is close enough to zero for the test to pass
    assertEquals(minimizeFunction.get(0), 0, 1E-5);
    assertEquals(minimizeFunction.get(1), 0, 1E-5);
  }

  @Test
  public void testMomentumGradientDescent() {

    DoubleVector start = new DenseDoubleVector(new double[] { 2, -1 });

    CostFunction inlineFunction = getCostFunction();
    GradientDescent gd = GradientDescentBuilder.create(0.8d).momentum(0.9d)
        .breakOnDifference(1e-20).build();
    DoubleVector minimizeFunction = gd.minimize(inlineFunction, start, 1000,
        false);
    // 1E-5 is close enough to zero for the test to pass
    assertEquals(minimizeFunction.get(0), 0, 1E-5);
    assertEquals(minimizeFunction.get(1), 0, 1E-5);
  }

  CostFunction getCostFunction() {
    // our function is f(x,y) = x^2+y^2
    // the derivative is f'(x,y) = 2x+2y
    CostFunction inlineFunction = new CostFunction() {
      @Override
      public CostGradientTuple evaluateCost(DoubleVector input) {

        double cost = Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);
        DenseDoubleVector gradient = new DenseDoubleVector(new double[] {
            input.get(0) * 2, input.get(1) * 2 });

        return new CostGradientTuple(cost, gradient);
      }
    };
    return inlineFunction;
  }

}
