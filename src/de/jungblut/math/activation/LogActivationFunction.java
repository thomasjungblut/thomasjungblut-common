package de.jungblut.math.activation;

/**
 * Log activation function, guarded against NaN and infinity edge cases.
 * 
 * @author thomas.jungblut
 * 
 */
public final class LogActivationFunction extends AbstractActivationFunction {

  @Override
  public double apply(double input) {
    return input >= 0 ? Math.log(1d + input) : -Math.log(1d - input);
  }

  @Override
  public double gradient(double input) {
    return input >= 0 ? 1d / (1d + input) : 1d / (1d - input);
  }

}
