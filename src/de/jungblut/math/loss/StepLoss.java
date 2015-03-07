package de.jungblut.math.loss;

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
public class StepLoss implements LossFunction {

  @Override
  public double calculateLoss(DoubleMatrix y, DoubleMatrix hypothesis) {
    return y.subtract(hypothesis).sum() / y.getRowCount();
  }

  @Override
  public double calculateLoss(DoubleVector y, DoubleVector hypothesis) {
    return y.subtract(hypothesis).sum();
  }

  @Override
  public DoubleVector calculateDerivative(DoubleVector y,
      DoubleVector hypothesis) {
    return new SingleEntryDoubleVector(y.subtract(hypothesis).sum());
  }

}
