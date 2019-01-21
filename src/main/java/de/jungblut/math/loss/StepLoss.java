package de.jungblut.math.loss;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.MathUtils;
import de.jungblut.math.activation.StepActivationFunction;
import de.jungblut.math.sparse.SequentialSparseDoubleVector;

import java.util.Iterator;

/**
 * Calculates a step error function that can be used for
 * {@link StepActivationFunction}.
 *
 * @author thomas.jungblut
 */
public class StepLoss implements LossFunction {

    @Override
    public double calculateLoss(DoubleMatrix y, DoubleMatrix hypothesis) {
        return y.subtract(hypothesis).sum() / y.getRowCount();
    }

    @Override
    public double calculateLoss(DoubleVector y, DoubleVector hypothesis) {
        return y.subtract(hypothesis).sum();
    }

    @Override
    public DoubleVector calculateGradient(DoubleVector feature, DoubleVector y,
                                          DoubleVector hypothesis) {

        double error = y.subtract(hypothesis).sum();
        if (error != 0d) {
            DoubleVector result = feature.deepCopy();
            Iterator<DoubleVectorElement> iterateNonZero = feature.iterateNonZero();
            while (iterateNonZero.hasNext()) {
                DoubleVectorElement next = iterateNonZero.next();
                result.set(next.getIndex(),
                        MathUtils.guardedLogarithm(next.getValue() + 1d) * error * -1d);
            }
            return result;
        }
        return new SequentialSparseDoubleVector(feature.getDimension());
    }
}
