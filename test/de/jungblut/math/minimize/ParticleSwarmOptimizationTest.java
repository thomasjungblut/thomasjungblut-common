package de.jungblut.math.minimize;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.math.DoubleMath;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public class ParticleSwarmOptimizationTest extends TestCase {

  @Test
  public void testParticleSwarmOptimization() {

    DoubleVector start = new DenseDoubleVector(new double[] { 22, 15 });

    // our function is f(x,y) = x^2+y^2
    CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

        double cost = Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);

        return new Tuple<>(cost, null);
      }
    };

    DoubleVector minimizeFunction = ParticleSwarmOptimization.minimizeFunction(
        inlineFunction, start, 1000, 0.1, 0.2, 0.4, 100, 8, false);
    // 1E-5 is close enough to zero for the test to pass
    assertEquals(0, DoubleMath.fuzzyCompare(minimizeFunction.get(0), 0, 1E-5));
    assertEquals(0, DoubleMath.fuzzyCompare(minimizeFunction.get(1), 0, 1E-5));
  }

  @Test
  public void testParticleSwarmOptimizationNonConvex() {

    DoubleVector start = new DenseDoubleVector(new double[] { 100, 30 });

    CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {
        double x = input.get(0);
        double y = input.get(1);
        // that's the rosenbrock function
        double cost = Math.pow((1 - x), 2) + 100 * Math.pow((y - x * x), 2);

        return new Tuple<>(cost, null);
      }
    };

    DoubleVector minimizeFunction = ParticleSwarmOptimization.minimizeFunction(
        inlineFunction, start, 1000, 2.8, 0.4, 0.8, 65, 8, false);

    assertEquals(0, DoubleMath.fuzzyCompare(minimizeFunction.get(0), 1d, 0.2));
    assertEquals(0, DoubleMath.fuzzyCompare(minimizeFunction.get(1), 1d, 0.2));
  }

}
