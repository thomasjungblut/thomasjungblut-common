package de.jungblut.math.activation;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.math.MathUtils;

/**
 * Smoothed approximation to a {@link ReluActivationFunction}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class SoftplusReluActivationFunction extends
    AbstractActivationFunction {

  @Override
  public double apply(double input) {
    return MathUtils.guardedLogarithm(1 + FastMath.exp(input));
  }

  @Override
  public double gradient(double input) {
    return 1d / (1 + FastMath.exp(-input));
  }

}
