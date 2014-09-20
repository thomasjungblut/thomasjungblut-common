package de.jungblut.math.squashing;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.LinearActivationFunction;

/**
 * Squared mean error function for regression problems and
 * {@link LinearActivationFunction}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class SquaredMeanErrorFunction implements ErrorFunction {

  @Override
  public double calculateError(DoubleMatrix y, DoubleMatrix hypothesis) {
    double sum = 0d;
    for (int col = 0; col < y.getColumnCount(); col++) {
      for (int row = 0; row < y.getRowCount(); row++) {
        double diff = y.get(row, col) - hypothesis.get(row, col);
        sum += (diff * diff);
      }
    }
    return sum / y.getRowCount();
  }

  @Override
  public double calculateError(DoubleVector y, DoubleVector hypothesis) {
    double sum = 0d;
    for (int col = 0; col < y.getDimension(); col++) {
      double diff = y.get(col) - hypothesis.get(col);
      sum += (diff * diff);
    }
    return sum;
  };

}
