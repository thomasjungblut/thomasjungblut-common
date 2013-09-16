package de.jungblut.math.activation;

import java.util.Iterator;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;

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
    if (vector.isSparse()) {
      Iterator<DoubleVectorElement> iterateNonZero = vector.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        subtract.set(next.getIndex(), Math.exp(subtract.get(next.getIndex())));
      }
    } else {
      for (int i = 0; i < subtract.getLength(); i++) {
        subtract.set(i, Math.exp(subtract.get(i)));
      }
    }
    return subtract.divide(subtract.sum());
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
