package de.jungblut.math.activation;

/**
 * Singleton helper to get the activation functions as singleton. It uses their
 * ordinal value and look the singletons in an pre-instantiated array.
 * 
 * @author thomas.jungblut
 * 
 */
public enum ActivationFunctionSelector {

  LINEAR, LOG, SIGMOID, SOFTMAX, TANH;

  private static final ActivationFunction[] FUNCTIONS = new ActivationFunction[] {
      new LinearActivationFunction(), new LogActivationFunction(),
      new SigmoidActivationFunction(), new SoftMaxActivationFunction(),
      new TanhActivationFunction() };

  public ActivationFunction get() {
    return FUNCTIONS[this.ordinal()];
  }

}
