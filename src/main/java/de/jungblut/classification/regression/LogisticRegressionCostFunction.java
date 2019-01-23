package de.jungblut.classification.regression;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.loss.LogLoss;
import de.jungblut.math.loss.LossFunction;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.CostGradientTuple;

import java.util.Arrays;

import static de.jungblut.math.activation.ActivationFunctionSelector.SIGMOID;

public final class LogisticRegressionCostFunction implements CostFunction {

    private static final LossFunction ERROR_FUNCTION = new LogLoss();

    private final DoubleMatrix x;
    private final DoubleMatrix xTransposed;
    private final DoubleMatrix y;
    private final int m;
    private final double lambda;

    /**
     * @param x      normal feature matrix, column 0 should contain the bias.
     * @param y      normal outcome matrix, for multiple classes use the one-hot
     *               encoding. This matrix should be transposed.
     * @param lambda l1 reg parameter.
     */
    public LogisticRegressionCostFunction(DoubleMatrix x, DoubleMatrix y,
                                          double lambda) {
        this.x = x;
        this.lambda = lambda;
        this.m = x.getRowCount();
        this.xTransposed = this.x.transpose();
        this.y = y;
    }

    @Override
    public CostGradientTuple evaluateCost(DoubleVector theta) {

        DoubleVector activation = SIGMOID.get().apply(x.multiplyVectorRow(theta));
        DenseDoubleMatrix hypo = new DenseDoubleMatrix(Arrays.asList(activation));
        double error = ERROR_FUNCTION.calculateLoss(y, hypo);
        DoubleMatrix loss = hypo.subtract(y);
        double j = error / m;
        DoubleVector gradient = xTransposed.multiplyVectorRow(loss.getRowVector(0))
                .divide(m);
        if (lambda != 0d) {
            DoubleVector reg = theta.multiply(lambda / m);
            // don't regularize the bias
            reg.set(0, 0d);
            gradient = gradient.add(reg);
            j += lambda * theta.pow(2).sum() / m;
        }

        return new CostGradientTuple(j, gradient);
    }
}
