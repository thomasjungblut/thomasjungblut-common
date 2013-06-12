package de.jungblut.math.squashing;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.MathUtils;

/**
 * Logistic error function implementation.
 * 
 * @author thomas.jungblut
 * 
 */
public final class LogisticErrorFunction implements ErrorFunction {

  @Override
  public double calculateError(DoubleMatrix y, DoubleMatrix hypothesis) {
    return (y.multiply(-1d)
        .multiplyElementWise(MathUtils.logMatrix(hypothesis)).subtract((y
        .subtractBy(1.0d)).multiplyElementWise(MathUtils.logMatrix(hypothesis
        .subtractBy(1d))))).sum();
  }

}
