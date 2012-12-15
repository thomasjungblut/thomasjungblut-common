package de.jungblut.classification.nn;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.cuda.JCUDAMatrixUtils;
import de.jungblut.math.dense.DenseDoubleMatrix;

/**
 * Neural network costfunction for a multilayer perceptron. Same as
 * {@link MultilayerPerceptronCostFunction}, but executes critical parts, e.G.
 * the matrix multiplications on the GPU to get a huge speedup. <br/>
 */
public final class GPUMultilayerPerceptronCostFunction extends
    MultilayerPerceptronCostFunction {

  public GPUMultilayerPerceptronCostFunction(MultilayerPerceptron network,
      DenseDoubleMatrix x, DenseDoubleMatrix y, double lambda) {
    super(network, x, y, lambda);
  }

  @Override
  protected DoubleMatrix multiply(DenseDoubleMatrix a1, DenseDoubleMatrix a2,
      boolean a1Transpose, boolean a2Transpose) {
    return JCUDAMatrixUtils.multiply(a1, a2, a1Transpose, a2Transpose);
  }

}
