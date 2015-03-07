package de.jungblut.math.activation;

/**
 * Classic perceptron-like step function. If the given input is greater than the
 * threshold it will emit 1, else 0.
 * 
 * @author thomas.jungblut
 *
 */
public class StepActivationFunction extends AbstractActivationFunction {

  private final double threshold;

  public StepActivationFunction(double threshold) {
    this.threshold = threshold;
  }

  @Override
  public double apply(double input) {
    return input > threshold ? 1 : 0;
  }

  @Override
  public double gradient(double input) {
    return 0;
  }

}
