package de.jungblut.math.loss;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.MathUtils;
import de.jungblut.math.activation.SoftMaxActivationFunction;

/**
 * Cross entropy error function, for example to be used with the
 * {@link SoftMaxActivationFunction}.
 *
 * @author thomas.jungblut
 */
public final class CrossEntropyLoss implements LossFunction {

    @Override
    public double calculateLoss(DoubleMatrix y, DoubleMatrix hypothesis) {
        return y.multiplyElementWise(MathUtils.logMatrix(hypothesis)).sum()
                / y.getRowCount();
    }

    @Override
    public double calculateLoss(DoubleVector y, DoubleVector hypothesis) {
        return y.multiply(MathUtils.logVector(hypothesis)).sum();
    }

    @Override
    public DoubleVector calculateGradient(DoubleVector feature, DoubleVector y,
                                          DoubleVector hypothesis) {
        return feature.multiply(hypothesis.subtract(y).get(0));
    }

}
