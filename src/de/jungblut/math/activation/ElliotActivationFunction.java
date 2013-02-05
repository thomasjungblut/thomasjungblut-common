package de.jungblut.math.activation;

/**
 * Implementation of the elliot activation function.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ElliotActivationFunction extends AbstractActivationFunction {

  @Override
  public double apply(double input) {
    return elliot(input);
  }

  @Override
  public double gradient(double input) {
    return elliotGradient(input);
  }

  static double elliot(double input) {
    return (input / 2d) / (1d + Math.abs(input)) + 0.5d;
  }

  static double elliotGradient(double input) {
    double denom = 1d + Math.abs(input);
    return 1d / (denom * denom) * 2;
  }

}
