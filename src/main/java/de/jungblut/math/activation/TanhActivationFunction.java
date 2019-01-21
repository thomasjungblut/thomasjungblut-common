package de.jungblut.math.activation;

import org.apache.commons.math3.util.FastMath;

/**
 * Implementation of the Tanh activation based on {@link FastMath}.
 *
 * @author thomas.jungblut
 */
public final class TanhActivationFunction extends AbstractActivationFunction {

    @Override
    public double apply(double input) {
        return FastMath.tanh(input);
    }

    @Override
    public double gradient(double input) {
        final double tanhX = FastMath.tanh(input);
        return 1d - tanhX * tanhX;
    }

}
