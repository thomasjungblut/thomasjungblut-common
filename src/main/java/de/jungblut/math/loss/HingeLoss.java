package de.jungblut.math.loss;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import org.apache.commons.math3.util.FastMath;

/**
 * Hinge-loss for linear SVMs. This needs the outcome class to be -1 for a
 * negative sample and 1 for a positive one.
 *
 * @author thomas.jungblut
 */
public class HingeLoss implements LossFunction {

    @Override
    public double calculateLoss(DoubleMatrix y, DoubleMatrix hypothesis) {
        DoubleMatrix multiplyElementWise = y.multiplyElementWise(hypothesis);
        double sum = 0d;
        for (int i = 0; i < multiplyElementWise.getRowCount(); i++) {
            sum += FastMath.max(0, 1 - multiplyElementWise.get(i, 0));
        }
        return sum / multiplyElementWise.getRowCount();
    }

    @Override
    public double calculateLoss(DoubleVector y, DoubleVector hypothesis) {
        DoubleVector v = y.multiply(hypothesis);
        return FastMath.max(0, 1 - v.get(0));
    }

    @Override
    public DoubleVector calculateGradient(DoubleVector feature, DoubleVector y,
                                          DoubleVector hypothesis) {

        DoubleVector v = y.multiply(hypothesis);
        if (v.get(0) > 1) {
            return feature.multiply(0);
        } else {
            return feature.multiply(y.multiply(-1).get(0));
        }
    }
}
