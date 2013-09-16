package de.jungblut.classification.regression;

import static de.jungblut.math.activation.ActivationFunctionSelector.SIGMOID;

import java.util.Arrays;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.CostGradientTuple;
import de.jungblut.math.squashing.ErrorFunction;
import de.jungblut.math.squashing.LogisticErrorFunction;

public final class LogisticRegressionCostFunction implements CostFunction {

  private static final ErrorFunction ERROR_FUNCTION = new LogisticErrorFunction();

  private final DoubleMatrix x;
  private final DoubleMatrix xTransposed;
  private final DoubleMatrix y;
  private final int m;

  /**
   * @param x normal feature matrix, column 0 should contain the bias.
   * @param y normal outcome matrix, for multiple classes use the one-hot
   *          encoding. This matrix should be transposed.
   * @param lambda reg parameter, currently not used.
   */
  public LogisticRegressionCostFunction(DoubleMatrix x, DoubleMatrix y,
      double lambda) {
    this.x = x;
    this.m = x.getRowCount();
    this.xTransposed = this.x.transpose();
    this.y = y;
  }

  @Override
  public CostGradientTuple evaluateCost(DoubleVector input) {

    DoubleVector activation = SIGMOID.get().apply(x.multiplyVectorRow(input));
    DenseDoubleMatrix hypo = new DenseDoubleMatrix(Arrays.asList(activation));
    double error = ERROR_FUNCTION.calculateError(y, hypo);
    DoubleMatrix loss = hypo.subtract(y);
    double j = error / m;
    DoubleVector gradient = xTransposed.multiplyVectorRow(loss.getRowVector(0))
        .divide(m);

    return new CostGradientTuple(j, gradient);
  }
}
