package de.jungblut.classification.nn;

import com.google.common.base.Preconditions;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.tuple.Tuple;

/**
 * Neural network costfunction for a single hidden layer perceptron.
 * 
 */
public class MultilayerPerceptronCostFunction implements CostFunction {

  private final DenseDoubleMatrix x;
  private final DenseDoubleMatrix y;
  private final double lambda;

  private final int m;
  private final int hiddenLayerSize;
  private final int inputLayerSize;
  private final int outputLayerSize;

  public MultilayerPerceptronCostFunction(MultilayerPerceptron network,
      DenseDoubleMatrix x, DenseDoubleMatrix y, double lambda) {
    this.m = x.getRowCount();
    this.x = new DenseDoubleMatrix(DenseDoubleVector.ones(m), x);
    this.y = y;
    this.lambda = lambda;
    // only works for 3 layer networks
    Preconditions.checkArgument(network.getLayers().length == 3);
    this.hiddenLayerSize = network.getLayers()[1].getLength();
    this.inputLayerSize = network.getLayers()[0].getLength();
    this.outputLayerSize = network.getLayers()[2].getLength();
  }

  /**
   * Input contains the network parameters (weights) as a folded vector. Code
   * mainly taken from ml-class to work with the fmincg to optimize the theta
   * weights.
   */
  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {
    // TODO can be extended to work with n-layers
    DenseDoubleMatrix[] thetas = DenseMatrixFolder.unfoldMatrices(input,
        new int[][] { { hiddenLayerSize, (inputLayerSize + 1) },
            { outputLayerSize, (hiddenLayerSize + 1) } });

    DenseDoubleMatrix[] thetaGradients = new DenseDoubleMatrix[thetas.length];
    for (int i = 0; i < thetas.length; i++) {
      thetaGradients[i] = new DenseDoubleMatrix(thetas[i].getRowCount(),
          thetas[i].getColumnCount());
    }

    // forward propagate
    DenseDoubleMatrix a1 = DenseDoubleMatrix.copy(x);
    DoubleMatrix z2 = a1.multiply(thetas[0].transpose());
    DenseDoubleMatrix a2 = new DenseDoubleMatrix(DenseDoubleVector.ones(m),
        sigmoidMatrix((DenseDoubleMatrix) z2));

    DoubleMatrix z3 = a2.multiply(thetas[1].transpose());
    DenseDoubleMatrix a3 = sigmoidMatrix((DenseDoubleMatrix) z3);

    // only calculate the regularization term if lambda is not 0
    double regularization = 0.0d;
    if (lambda != 0.0d) {
      regularization = (lambda / (2.0d * m))
          * ((thetas[0].slice(0, thetas[0].getRowCount(), 1,
              thetas[0].getColumnCount())).pow(2).sum() + (thetas[1].slice(0,
              thetas[1].getRowCount(), 1, thetas[1].getColumnCount())).pow(2)
              .sum());
    }
    // calculate our cost function
    double j = (1.0d / m)
        * (y.multiply(-1).multiplyElementWise(logMatrix(a3)).subtract((y
            .subtractBy(1.0d)).multiplyElementWise(logMatrix(a3
            .subtractBy(1.0d))))).sum() + regularization;

    DoubleMatrix delta3 = a3.subtract(y);
    DoubleMatrix delta2 = delta3.multiply(
        thetas[1].slice(0, thetas[1].getRowCount(), 1,
            thetas[1].getColumnCount())).multiplyElementWise(
        sigmoidGradientMatrix((DenseDoubleMatrix) z2));

    // calculate our two gradients
    thetaGradients[0] = (DenseDoubleMatrix) (delta2.transpose().multiply(a1)
        .multiply(1.0d / m)).add((thetas[0].multiply(lambda / m)));

    thetaGradients[1] = (DenseDoubleMatrix) (delta3.transpose().multiply(a2)
        .multiply(1.0d / m)).add((thetas[1].multiply(lambda / m)));

    // subtract the regularized bias
    thetaGradients[0].setColumnVector(0,
        thetas[0].slice(0, thetas[0].getRowCount(), 0, 1).multiply(lambda / m)
            .getColumnVector(0));
    thetaGradients[1].setColumnVector(0,
        thetas[1].slice(0, thetas[1].getRowCount(), 0, 1).multiply(lambda / m)
            .getColumnVector(0));

    return new Tuple<Double, DoubleVector>(j,
        DenseMatrixFolder.foldMatrices(thetaGradients));
  }

  /*
   * some static helpers for sigmoid and log calculation
   */

  static double sigmoid(double input) {
    return 1.0 / (1.0 + Math.exp(-input));
  }

  static double sigmoidGradient(double input) {
    return sigmoid(input) * (1 - sigmoid(input));
  }

  static DenseDoubleMatrix sigmoidMatrix(DenseDoubleMatrix input) {
    DenseDoubleMatrix sigmoid = DenseDoubleMatrix.copy(input);
    for (int row = 0; row < sigmoid.getRowCount(); row++) {
      for (int col = 0; col < sigmoid.getColumnCount(); col++) {
        sigmoid.set(row, col, sigmoid(sigmoid.get(row, col)));
      }
    }
    return sigmoid;
  }

  static DenseDoubleMatrix sigmoidGradientMatrix(DenseDoubleMatrix input) {
    DenseDoubleMatrix sigmoid = DenseDoubleMatrix.copy(input);
    for (int row = 0; row < sigmoid.getRowCount(); row++) {
      for (int col = 0; col < sigmoid.getColumnCount(); col++) {
        sigmoid.set(row, col, sigmoidGradient(sigmoid.get(row, col)));
      }
    }
    return sigmoid;
  }

  static DenseDoubleMatrix logMatrix(DenseDoubleMatrix input) {
    DenseDoubleMatrix log = DenseDoubleMatrix.copy(input);
    for (int row = 0; row < log.getRowCount(); row++) {
      for (int col = 0; col < log.getColumnCount(); col++) {
        log.set(row, col, Math.log(log.get(row, col)));
      }
    }
    return log;
  }

}
