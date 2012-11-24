package de.jungblut.classification.nn;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.dense.DenseDoubleMatrix;

/**
 * Calculates the error, for example in the last layer of a neural net.
 * 
 * @author thomas.jungblut
 * 
 */
public enum ErrorFunction {

  SIGMOID_ERROR {

    @Override
    double getError(DoubleMatrix output, DoubleMatrix target) {
      return (output.multiply(-1d).multiplyElementWise(logMatrix(target))
          .subtract((output.subtractBy(1.0d))
              .multiplyElementWise(logMatrix(target.subtractBy(1d))))).sum();
    }
  },
  SOFTMAX_ERROR {
    // cross entropy
    @Override
    double getError(DoubleMatrix output, DoubleMatrix target) {
      return output.multiplyElementWise(logMatrix(target)).sum();
    }
  },
  SQUARED_MEAN_ERROR {
    @Override
    double getError(DoubleMatrix output, DoubleMatrix target) {
      double avg = 0d;
      for (int col = 0; col < output.getColumnCount(); col++) {
        for (int row = 0; row < output.getRowCount(); row++) {
          double diff = output.get(row, col) - target.get(row, col);
          avg += (diff * diff);
        }
      }
      return avg / output.getRowCount();
    }
  };

  abstract double getError(DoubleMatrix y, DoubleMatrix hypothesis);

  // helper on calculating the log of every element of the matrix
  static DoubleMatrix logMatrix(DoubleMatrix input) {
    DenseDoubleMatrix log = new DenseDoubleMatrix(input.getRowCount(),
        input.getColumnCount());
    for (int row = 0; row < log.getRowCount(); row++) {
      for (int col = 0; col < log.getColumnCount(); col++) {
        log.set(row, col, Math.log(input.get(row, col)));
      }
    }
    return log;
  }

}
