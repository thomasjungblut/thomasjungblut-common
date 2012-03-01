package de.jungblut.neural;

import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.util.Tuple;

public class NNCostFunction implements CostFunction {

  private final int inputLayerSize;
  private final int hiddenLayerSize;
  private final int numLabels;
  private final DenseDoubleMatrix x;
  private final DenseDoubleVector y;
  private final double lambda;
  private final int m;
  private int[][] foldArrays;

  public NNCostFunction(int inputLayerSize, int hiddenLayerSize, int numLabels,
      DenseDoubleMatrix x, DenseDoubleVector y, double lambda) {
    super();
    this.inputLayerSize = inputLayerSize;
    this.hiddenLayerSize = hiddenLayerSize;
    this.numLabels = numLabels;
    this.x = x;
    this.y = y;
    this.lambda = lambda;
    this.m = x.getRowCount();
    // TODO second argument is not correct, it needs 3 arguments at least
    foldArrays = new int[][] { { hiddenLayerSize*(inputLayerSize+1), hiddenLayerSize },
        { hiddenLayerSize*(inputLayerSize+1), hiddenLayerSize } };
  }

  @Override
  public Tuple<Double, DenseDoubleVector> evaluateCost(DenseDoubleVector input) {
    // unroll thetas
    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(input, foldArrays);
    DenseDoubleMatrix theta1 = unfoldMatrices[0];
    DenseDoubleMatrix theta2 = unfoldMatrices[1];
    
    

    return null;
  }

  private DenseDoubleMatrix sigmoid(DenseDoubleMatrix in) {
    DenseDoubleMatrix toReturn = new DenseDoubleMatrix(in.getRowCount(),
        in.getColumnCount());

    for (int row = 0; row < in.getRowCount(); row++) {
      for (int col = 0; col < in.getColumnCount(); col++) {
        toReturn.set(row, col,
            (1.0d / Math.pow(Math.E, -in.get(row, col)) + 1.0));
      }
    }
    return toReturn;
  }
  
  private DenseDoubleMatrix sigmoidGradient(DenseDoubleMatrix in) {
    DenseDoubleMatrix sigmoid = sigmoid(in);
    return sigmoid.multiplyElementWise(sigmoid.subtractBy(1.0));
  }

}
