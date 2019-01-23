package de.jungblut.math.activation;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;

/**
 * Linear activation function. Basic proxy of the incoming values. Thus
 * implementing the matrix and vector implementations with the return of the
 * input.
 *
 * @author thomas.jungblut
 */
public final class LinearActivationFunction extends AbstractActivationFunction {

    @Override
    public double apply(double input) {
        return input;
    }

    @Override
    public double gradient(double input) {
        return 1d;
    }

    @Override
    public DoubleVector apply(DoubleVector vector) {
        return vector;
    }

    @Override
    public DoubleMatrix apply(DoubleMatrix matrix) {
        return matrix;
    }

    @Override
    public DoubleVector gradient(DoubleVector vector) {
        return vector;
    }

    @Override
    public DoubleMatrix gradient(DoubleMatrix matrix) {
        return matrix;
    }

}
