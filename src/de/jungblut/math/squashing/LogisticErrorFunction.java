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

    DoubleMatrix negativeOutcome = y.subtractBy(1.0d);
    DoubleMatrix inverseOutcome = y.multiply(-1d);
    DoubleMatrix negativeHypo = hypothesis.subtractBy(1d);
    DoubleMatrix negativeLogHypo = MathUtils.logMatrix(negativeHypo);
    DoubleMatrix positiveLogHypo = MathUtils.logMatrix(hypothesis);
    DoubleMatrix negativePenalty = negativeOutcome
        .multiplyElementWise(negativeLogHypo);
    DoubleMatrix positivePenalty = inverseOutcome
        .multiplyElementWise(positiveLogHypo);

    return (positivePenalty.subtract(negativePenalty)).sum();
  }
}
