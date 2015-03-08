package de.jungblut.math.loss;

import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.activation.StepActivationFunction;

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
  public DoubleVector calculateGradient(DoubleVector feature, DoubleVector y,
      DoubleVector hypothesis) {

    double error = y.subtract(hypothesis).sum();
    DoubleVector result = feature.deepCopy();
    if (error != 0d) {
      Iterator<DoubleVectorElement> iterateNonZero = feature.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        result.set(next.getIndex(), FastMath.log(next.getValue() + 1d) * error);
      }
    }
    return result;
  }
}
