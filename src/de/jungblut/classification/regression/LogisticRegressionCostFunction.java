package de.jungblut.classification.regression;

import static de.jungblut.math.activation.ActivationFunctionSelector.SIGMOID;

import java.util.Collections;

import de.jungblut.classification.nn.ErrorFunction;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
import de.jungblut.math.tuple.Tuple;

/**
 * Logistic regression cost function to optimize with an arbitrary
 * {@link Minimizer}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class LogisticRegressionCostFunction implements CostFunction {

  private static final ErrorFunction ERROR_FUNCTION = ErrorFunction.SIGMOID_ERROR;

  private final DoubleMatrix x;
  private final DenseDoubleMatrix y;
  private final double lambda;
  private final int m;

  private DenseDoubleMatrix eye;

  public LogisticRegressionCostFunction(DoubleMatrix x, DoubleVector y,
      double lambda) {
    if (!x.isSparse()) {
      // add a column of ones to handle the intercept term
      this.x = new DenseDoubleMatrix(DenseDoubleVector.ones(y.getLength()), x);
    } else {
      this.x = new SparseDoubleRowMatrix(DenseDoubleVector.ones(y.getLength()),
          x);
    }
    this.y = new DenseDoubleMatrix(Collections.singletonList(y));
    this.lambda = lambda;
    this.m = y.getLength();
    eye = DenseDoubleMatrix.eye(x.getColumnCount() + 1);
    eye.set(0, 0, 0.0d);
  }

  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

    double reg = input.slice(1, input.getLength()).pow(2).sum() * lambda
        / (2.0d * m);
    DenseDoubleMatrix hypo = new DenseDoubleMatrix(
        Collections.singletonList(SIGMOID.get().apply(
            x.multiplyVectorRow(input))));
    double sum = ERROR_FUNCTION.getError(y, hypo);
    double j = (1.0d / m) * sum + reg;

    DoubleVector regGradient = eye.multiply(lambda).multiplyVectorRow(input);
    DoubleVector gradient = x.transpose()
        .multiplyVectorRow(hypo.subtract(y).getRowVector(0)).add(regGradient)
        .divide(m);

    return new Tuple<>(j, gradient);
  }
}
