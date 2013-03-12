package de.jungblut.math.minimize;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.math.DoubleMath;

import de.jungblut.datastructure.CollectionInputProvider;
import de.jungblut.datastructure.InputProvider;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.MathUtils;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public class StochasticGradientDescentTest extends TestCase {

  @Test
  public void testSingleItemStochasticGradientDescent() {

    DoubleVector start = new DenseDoubleVector(new double[] { 212, 200 });

    // our function is f(x,y) = x^2+y^2
    // the derivative is f'(x,y) = 2x+2y
    final CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

        double cost = Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);
        DenseDoubleVector gradient = new DenseDoubleVector(new double[] {
            input.get(0) * 2, input.get(1) * 2 });

        return new Tuple<Double, DoubleVector>(cost, gradient);
      }
    };

    StochasticCostFunction f = new StochasticCostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input,
          DoubleVector x, DenseDoubleVector y) {

        DoubleVector prediction = x.multiply(input);
        DoubleVector diff = prediction.subtract(y).abs();
        double cost = diff.sum();

        DoubleVector gradient = MathUtils.numericalGradient(prediction,
            inlineFunction);

        return new Tuple<>(cost, gradient);
      }
    };

    // sample a grid between 0,0 and 100,100
    ArrayList<Tuple<DoubleVector, DenseDoubleVector>> data = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      for (int k = 0; k < 100; k++) {
        DenseDoubleVector inX = new DenseDoubleVector(new double[] { i, k });
        data.add(new Tuple<DoubleVector, DenseDoubleVector>(inX,
            new DenseDoubleVector(new double[] { inlineFunction.evaluateCost(
                inX).getFirst() })));
      }
    }
    Collections.shuffle(data);
    InputProvider<Tuple<DoubleVector, DenseDoubleVector>> provider = new CollectionInputProvider<>(
        data);

    DoubleVector minimizeFunction = StochasticGradientDescent.minimizeFunction(
        f, provider, start, 0.001, 1e-10, 1000, false);
    // 1E-5 is close enough to zero for the test to pass
    System.out.println(minimizeFunction);
    assertEquals(0, DoubleMath.fuzzyCompare(minimizeFunction.get(0), 0, 1E-5));
    assertEquals(0, DoubleMath.fuzzyCompare(minimizeFunction.get(1), 0, 1E-5));
  }

}
