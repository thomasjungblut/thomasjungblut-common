package de.jungblut.math.minimize;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public class ParticleSwarmOptimizationTest extends TestCase {

  @Test
  public void testParticleSwarmOptimization() {

    DoubleVector start = new DenseDoubleVector(new double[] { 2, 5 });

    // our function is f(x,y) = x^2+y^2
    CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

        double cost = Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);

        return new Tuple<Double, DoubleVector>(cost, null);
      }
    };

    DoubleVector minimizeFunction = ParticleSwarmOptimization.minimizeFunction(
        inlineFunction, start, 1000, 0.1, 0.2, 0.4, 100, false);
    // 1E-5 is close enough to zero for the test to pass
    assertTrue(minimizeFunction.get(0) >= 0 && minimizeFunction.get(0) < 1E-5);
    assertTrue(minimizeFunction.get(1) >= 0 && minimizeFunction.get(1) < 1E-5);
  }

}
