package de.jungblut.math.activation;

import org.junit.Assert;
import org.junit.Test;

public class SigmoidActivationFunctionTest {

  @Test
  public void testSigmoidClippingPositive() {
    Assert.assertEquals(1d, SigmoidActivationFunction.sigmoid(35), 1e-4);
  }

  @Test
  public void testSigmoidClippingNegative() {
    Assert.assertEquals(0d, SigmoidActivationFunction.sigmoid(-35), 1e-4);
  }

  @Test
  public void testSigmoidHappyPath() {
    Assert.assertEquals(0.0067, SigmoidActivationFunction.sigmoid(-5), 1e-4);
    Assert.assertEquals(0.2689, SigmoidActivationFunction.sigmoid(-1), 1e-4);
    Assert.assertEquals(0.5d, SigmoidActivationFunction.sigmoid(0), 1e-4);
    Assert.assertEquals(0.7310d, SigmoidActivationFunction.sigmoid(1), 1e-4);
    Assert.assertEquals(0.9933, SigmoidActivationFunction.sigmoid(5), 1e-4);
  }

  @Test
  public void testSigmoidGradientHappyPath() {
    Assert.assertEquals(0.1966, SigmoidActivationFunction.sigmoidGradient(-1),
        1e-4);
    Assert.assertEquals(0.25d, SigmoidActivationFunction.sigmoidGradient(0),
        1e-4);
    Assert.assertEquals(0.1966, SigmoidActivationFunction.sigmoidGradient(1),
        1e-4);
  }

}
