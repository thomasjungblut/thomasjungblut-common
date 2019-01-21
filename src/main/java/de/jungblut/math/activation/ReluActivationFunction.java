package de.jungblut.math.activation;

import org.apache.commons.math3.util.FastMath;

/**
 * Rectified linear units implementation.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ReluActivationFunction extends AbstractActivationFunction {

  @Override
  public double apply(double input) {
    return FastMath.max(0, input);
  }

  @Override
  public double gradient(double input) {
    return input > 0 ? 1 : 0;
  }

}
