package de.jungblut.math.loss;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import org.apache.commons.math3.util.FastMath;

/**
 * MAE for regression problems.
 *
 * @author thomas.jungblut
 */
public final class MeanAbsoluteLoss implements LossFunction {

    @Override
    public double calculateLoss(DoubleMatrix y, DoubleMatrix hypothesis) {
        double sum = 0d;
        for (int col = 0; col < y.getColumnCount(); col++) {
            for (int row = 0; row < y.getRowCount(); row++) {
                sum += FastMath.abs(y.get(row, col) - hypothesis.get(row, col));
            }
        }
        return sum / y.getRowCount();
    }

    @Override
    public double calculateLoss(DoubleVector y, DoubleVector hypothesis) {
        double sum = 0d;
        for (int col = 0; col < y.getDimension(); col++) {
            sum += FastMath.abs(y.get(col) - hypothesis.get(col));
        }
        return sum;
    }

    @Override
    public DoubleVector calculateGradient(DoubleVector feature, DoubleVector y,
                                          DoubleVector hypothesis) {
        return feature.multiply(hypothesis.subtract(y).get(0));
    }

}
