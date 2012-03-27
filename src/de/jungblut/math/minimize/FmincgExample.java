package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public class FmincgExample {

  public static void main(String[] args) {
    int startPoint = -5;
    // start at x=-5
    DoubleVector start = new DenseDoubleVector(new double[] { startPoint });

    // our function is f(x) = (4-x)^2+10
    // the derivative is f(x)' = 2x-8
    CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

        double cost = Math.pow(4 - input.get(0), 2) + 10;
        DenseDoubleVector gradient = new DenseDoubleVector(
            new double[] { 2 * input.get(0) - 8 });

        return new Tuple<Double, DoubleVector>(cost, gradient);
      }
    };

    DoubleVector minimizeFunction = Fmincg.minimizeFunction(inlineFunction,
        start, 100, true);
    // should return 4
    System.out.println("Found a minimum at: " + minimizeFunction);

  }

}
