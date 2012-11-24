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
    DoubleVector subtract = vector.subtract(max);
    for (int i = 0; i < subtract.getLength(); i++) {
      subtract.set(i, Math.exp(subtract.get(i)));
    }
    return subtract.divide(subtract.sum());
  }

  @Override
  public DoubleMatrix apply(DoubleMatrix matrix) {
    DoubleMatrix dm = newInstance(matrix);
    for (int row = 0; row < matrix.getRowCount(); row++) {
      dm.setRowVector(row, apply(matrix.getRowVector(row)));
    }
    return dm;
  }

  @Override
  public double gradient(double input) {
    return 1d;
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
