package de.jungblut.math.activation;

import de.jungblut.classification.nn.MultilayerPerceptron;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;

/**
 * Squashing function interface to provide multiple activation functions, e.G.
 * in the {@link MultilayerPerceptron}.
 *
 * @author thomas.jungblut
 */
public interface ActivationFunction {

    /**
     * Applies the activation function on the given element.
     */
    public double apply(double input);

    /**
     * Applies the activation function on each element in the given vector.
     *
     * @param vector the vector to apply this function on.
     * @return a new vector that contains the activated elements.
     */
    public DoubleVector apply(DoubleVector vector);

    /**
     * Applies the gradient of the activation function on each element in the
     * given matrix.
     *
     * @param matrix the matrix to apply this function on.
     * @return a new matrix that contains the gradient of the elements.
     */
    public DoubleMatrix apply(DoubleMatrix matrix);

    /**
     * Applies the gradient of the activation function on the given element.
     */
    public double gradient(double input);

    /**
     * Applies the gradient of the activation function on each element in the
     * given vector.
     *
     * @param vector the vector to apply this function on.
     * @return a new vector that contains the gradient of the elements.
     */
    public DoubleVector gradient(DoubleVector vector);

    /**
     * Applies the gradient of the activation function on each element in the
     * given matrix.
     *
     * @param matrix the matrix to apply this function on.
     * @return a new matrix that contains the gradient of the elements.
     */
    public DoubleMatrix gradient(DoubleMatrix matrix);

}
