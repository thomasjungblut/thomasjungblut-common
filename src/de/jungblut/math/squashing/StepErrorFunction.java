package de.jungblut.math.squashing;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.StepActivationFunction;
import de.jungblut.math.dense.SingleEntryDoubleVector;

/**
 * Calculates a step error function that can be used for
 * {@link StepActivationFunction}.
 * 
 * @author thomas.jungblut
 *
 */
public class StepErrorFunction implements ErrorFunction {

  @Override
  public double calculateError(DoubleMatrix y, DoubleMatrix hypothesis) {
    return y.subtract(hypothesis).sum() / y.getRowCount();
  }

  @Override
  public double calculateError(DoubleVector y, DoubleVector hypothesis) {
    return y.subtract(hypothesis).sum();
  }

  @Override
  public DoubleVector calculateDerivative(DoubleVector y,
      DoubleVector hypothesis) {
    return new SingleEntryDoubleVector(y.subtract(hypothesis).sum());
  }

}
