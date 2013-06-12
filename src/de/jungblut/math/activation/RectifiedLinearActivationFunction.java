package de.jungblut.math.activation;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.math.MathUtils;

/**
 * Rectified linear units implementation.
 * 
 * @author thomas.jungblut
 * 
 */
public final class RectifiedLinearActivationFunction extends
    AbstractActivationFunction {

  @Override
  public double apply(double input) {
    return MathUtils.guardLogarithm(1 + FastMath.exp(input));
  }

  @Override
  public double gradient(double input) {
    return 1d / (1 + FastMath.exp(-input));
  }

}
