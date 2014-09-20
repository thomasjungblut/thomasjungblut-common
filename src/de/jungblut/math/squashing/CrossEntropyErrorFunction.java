package de.jungblut.math.squashing;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.MathUtils;
import de.jungblut.math.activation.SoftMaxActivationFunction;

/**
 * Cross entropy error function, for example to be used with the
 * {@link SoftMaxActivationFunction}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class CrossEntropyErrorFunction implements ErrorFunction {

  @Override
  public double calculateError(DoubleMatrix y, DoubleMatrix hypothesis) {
    return y.multiplyElementWise(MathUtils.logMatrix(hypothesis)).sum();
  }

  @Override
  public double calculateError(DoubleVector y, DoubleVector hypothesis) {
    return y.multiply(MathUtils.logVector(hypothesis)).sum();
  }

}
