package de.jungblut.math.minimize;

import de.jungblut.math.DenseDoubleVector;
import de.jungblut.util.Tuple;

public class Fmincg {

  private static final double RHO = 0.01; // a bunch of constants for line
                                          // searches
  private static final double SIG = 0.5; // RHO and SIG are the constants in the
                                         // Wolfe-Powell conditions
  private static final double INT = 0.1; // don't reevaluate within 0.1 of the
                                         // limit of the current bracket
  private static final double EXT = 3.0; // extrapolate maximum 3 times the
                                         // current bracket
  private static final int MAX = 20; // max 20 function evaluations per line
                                     // search
  private static final int RATIO = 100; // maximum allowed slope ratio

  public static DenseDoubleVector minimizeFunction(CostFunction f,
      DenseDoubleVector input, int length) {

    int M = 0;
    int i = 0; // zero the run length counter
    int red = 1; // starting point
    int ls_failed = 0; // no previous line search has failed
    DenseDoubleVector fX = new DenseDoubleVector(0); // what we return as fX
    // get function value and gradient
    final Tuple<Double, DenseDoubleVector> evaluateCost = f.evaluateCost(input);
    double f1 = evaluateCost.getFirst();
    DenseDoubleVector df1 = evaluateCost.getSecond();
    i = i + (length < 0 ? 1 : 0);
    DenseDoubleVector s = df1.multiply(-1.0d); // search direction is steepest

    double d1 = s.multiply(-1.0d).dot(s); // this is the slope
    double z1 = (double) red / (1.0 - d1); // initial step is red/(|s|+1)

    while (i < Math.abs(length)) {// while not finished
      i = i + (length > 0 ? 1 : 0);// count iterations?!
      // make a copy of current values
      DenseDoubleVector X0 = DenseDoubleVector.copy(input);
      double f0 = f1;
      DenseDoubleVector df0 = DenseDoubleVector.copy(df1);
      // begin line search
      input = input.add(s.multiply(z1));
      final Tuple<Double, DenseDoubleVector> evaluateCost2 = f
          .evaluateCost(input);
      double f2 = evaluateCost2.getFirst();
      DenseDoubleVector df2 = evaluateCost2.getSecond();

      i = i + (length < 0 ? 1 : 0); // count epochs?!
      double d2 = df2.dot(s);
      // initialize point 3 equal to point 1
      double f3 = f1;
      double d3 = d1;
      double z3 = -z1;
      if (length > 0) {
        M = MAX;
      } else {
        M = Math.min(MAX, -length - i);
      }
      // initialize quanteties
      int success = 0;
      double limit = -1;

      while (true) {
        while (((f2 > f1 + z1 * RHO * d1) | (d2 > -SIG * d1)) && (M > 0)) {
          limit = z1; // tighten the bracket
          double z2 = 0.0d;
          double A = 0.0d;
          double B = 0.0d;
          if (f2 > f1) {
            // quadratic fit
            z2 = z3 - (0.5 * d3 * z3 * z3) / (d3 * z3 + f2 - f3);
          } else {
            A = 6 * (f2 - f3) / z3 + 3 * (d2 + d3); // cubic fit
            B = 3 * (f3 - f2) - z3 * (d3 + 2 * d2);
            // numerical error possible - ok!
            z2 = (Math.sqrt(B * B - A * d2 * z3 * z3) - B) / A;
          }
          if (Double.isNaN(z2) || Double.isInfinite(z2)) {
            z2 = z3 / 2.0d; // if we had a numerical problem then bisect
          }
          // don't accept too close to limits
          z2 = Math.max(Math.min(z2, INT * z3), (1 - INT) * z3);
          z1 = z1 + z2; // update the step
          input = input.add(s.multiply(z2));
          final Tuple<Double, DenseDoubleVector> evaluateCost3 = f
              .evaluateCost(input);
          f2 = evaluateCost3.getFirst();
          df2 = evaluateCost3.getSecond();
          M = M - 1;
          i = i + (length < 0 ? 1 : 0); // count epochs?!
          d2 = df2.dot(s);
          z3 = z3 - z2; // z3 is now relative to the location of z2
        }
        if (f2 > f1 + z1 * RHO * d1 || d2 > -SIG * d1) {
          break; // this is a failure
        } else if (d2 > SIG * d1) {
          success = 1;
          break; // success
        } else if (M == 0) {
          break; // failure
        }
        double A = 6 * (f2 - f3) / z3 + 3 * (d2 + d3); // make cubic
                                                       // extrapolation
        double B = 3 * (f3 - f2) - z3 * (d3 + 2 * d2);
        double z2 = -d2 * z3 * z3 / (B + Math.sqrt(B * B - A * d2 * z3 * z3));
        // num prob or wrong sign?
        if (Double.isNaN(z2) || Double.isInfinite(z2) || z2 < 0)
          if (limit < -0.5) { // if we have no upper limit
            z2 = z1 * (EXT - 1); // the extrapolate the maximum amount
          } else {
            z2 = (limit - z1) / 2; // otherwise bisect
          }
        else if ((limit > -0.5) && (z2 + z1 > limit)) {
          // extraplation beyond max?
          z2 = (limit - z1) / 2; // bisect
        } else if ((limit < -0.5) && (z2 + z1 > z1 * EXT)) {
          // extrapolationbeyond limit
          z2 = z1 * (EXT - 1.0); // set to extrapolation limit
        } else if (z2 < -z3 * INT) {
          z2 = -z3 * INT;
        } else if ((limit > -0.5) && (z2 < (limit - z1) * (1.0 - INT))) {
          // too close to the limit
          z2 = (limit - z1) * (1.0 - INT);
        }
        // set point 3 equal to point 2
        f3 = f2;
        d3 = d2;
        z3 = -z2;
        z1 = z1 + z2;
        // update current estimates
        input = input.add(s.multiply(z2));
        final Tuple<Double, DenseDoubleVector> evaluateCost3 = f
            .evaluateCost(input);
        f2 = evaluateCost3.getFirst();
        df2 = evaluateCost3.getSecond();
        M = M - 1;
        i = i + (length < 0 ? 1 : 0); // count epochs?!
        d2 = df2.dot(s);
      }// end of line search

      DenseDoubleVector tmp = null;

      if (success == 1) { // if line search succeeded
        f1 = f2;
        fX = new DenseDoubleVector(fX.toArray(), f1);
        System.out.printf("Interation %d | Cost: %f\r", i, f1);
        // Polack-Ribiere direction
        // (df2'*df2-df1'*df2)/(df1'*df1)*s - df2;
        // TODO df1'*df1 = matrix not double...
        s = s.multiply(df1.dot(df1)).subtract(df2)
            .divideFrom((df2.dot(df2) - df1.dot(df2)));
        tmp = df1;
        df1 = df2;
        df2 = tmp; // swap derivatives
        d2 = df1.dot(s);
        if (d2 > 0) { // new slope must be negative
          s = df1.multiply(-1.0d); // otherwise use steepest direction
          d2 = s.multiply(-1.0d).dot(s);
        }
        // MIN_VALUE is actually realmin = 2.2251e-308, this will overflow
        // double.. d2-Double.MIN_VALUE
        z1 = z1 * Math.min(RATIO, d1 / (d2-2.2251e-308)); // slope ratio but max RATIO
        d1 = d2;
        ls_failed = 0; // this line search did not fail
      } else {
        input = X0;
        f1 = f0;
        df1 = df0; // restore point from before failed line search
        // line search failed twice in a row?
        if (ls_failed == 1 || i > Math.abs(length)) {
          break; // or we ran out of time, so we give up
        }
        tmp = df1;
        df1 = df2;
        df2 = tmp; // swap derivatives
        s = df1.multiply(-1.0d); // try steepest
        d1 = s.multiply(-1.0d).dot(s);
        z1 = 1.0d / (1.0d - d1);
        ls_failed = 1; // this line search failed
      }

    }

    return input;
  }
}
