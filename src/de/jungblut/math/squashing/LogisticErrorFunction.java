package de.jungblut.math.squashing;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
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

  @Override
  public double calculateError(DoubleVector y, DoubleVector hypothesis) {

    DoubleVector negativeOutcome = y.subtractFrom(1.0d);
    DoubleVector inverseOutcome = y.multiply(-1d);
    DoubleVector negativeHypo = hypothesis.subtractFrom(1d);
    DoubleVector negativeLogHypo = MathUtils.logVector(negativeHypo);
    DoubleVector positiveLogHypo = MathUtils.logVector(hypothesis);
    DoubleVector negativePenalty = negativeOutcome.multiply(negativeLogHypo);
    DoubleVector positivePenalty = inverseOutcome.multiply(positiveLogHypo);

    return (positivePenalty.subtract(negativePenalty)).sum();
  }

  @Override
  public DoubleVector calculateDerivative(DoubleVector y,
      DoubleVector hypothesis) {
    return hypothesis.subtract(y);
  }
}
