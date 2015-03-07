package de.jungblut.math.squashing;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.SingleEntryDoubleVector;

/**
 * Hinge-loss for linear SVMs. This needs the outcome class to be -1 for a
 * negative sample and 1 for a positive one.
 * 
 * @author thomas.jungblut
 *
 */
public class HingeErrorFunction implements ErrorFunction {

  @Override
  public double calculateError(DoubleMatrix y, DoubleMatrix hypothesis) {
    DoubleMatrix multiplyElementWise = y.multiplyElementWise(hypothesis);
    double sum = 0d;
    for (int i = 0; i < multiplyElementWise.getRowCount(); i++) {
      sum += FastMath.max(0, 1 - multiplyElementWise.get(i, 0));
    }
    return sum / multiplyElementWise.getRowCount();
  }

  @Override
  public double calculateError(DoubleVector y, DoubleVector hypothesis) {
    DoubleVector v = y.multiply(hypothesis);
    return FastMath.max(0, 1 - v.get(0));
  }

  @Override
  public DoubleVector calculateDerivative(DoubleVector y,
      DoubleVector hypothesis) {

    DoubleVector v = y.multiply(hypothesis);
    if (v.get(0) > 1) {
      return new SingleEntryDoubleVector(0d);
    } else {
      return y.multiply(-1);
    }
  }
}
