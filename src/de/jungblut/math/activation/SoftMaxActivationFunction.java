package de.jungblut.math.activation;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;

/**
 * Softmax activation that only works on vectors, because it needs to sum and
 * divide the probabilities. In the case of matrices, the row vectors are taken.
 * 
 * @author thomas.jungblut
 * 
 */
public final class SoftMaxActivationFunction extends AbstractActivationFunction {

  @Override
  public double apply(double input) {
    return input;
  }

  @Override
  public DoubleVector apply(DoubleVector vector) {
    double max = vector.max();
    DoubleVector exp = vector.subtract(max).exp();
    return exp.divide(exp.sum());
  }

  @Override
  public DoubleMatrix apply(DoubleMatrix matrix) {
    DoubleMatrix dm = newInstance(matrix);
    for (int row = 0; row < matrix.getRowCount(); row++) {
      DoubleVector apply = apply(matrix.getRowVector(row));
      if (apply.getLength() != 0) {
        dm.setRowVector(row, apply);
      }
    }
    return dm;
  }

  @Override
  public double gradient(double input) {
    return input;
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
