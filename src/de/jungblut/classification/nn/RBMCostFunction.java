package de.jungblut.classification.nn;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.activation.ActivationFunctionSelector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.function.DoubleVectorFunction;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.tuple.Tuple;

/**
 * Restricted Boltzmann machine implementation using Contrastive Divergence 1
 * (CD1). This algorithm is based on what has been teached by Prof. Hinton in
 * the Coursera course "Neural Networks for Machine Learning" '12. This is an
 * unsupervised learning algorithm to train high level feature detectors.
 * 
 * @author thomas.jungblut
 * 
 */
public final class RBMCostFunction implements CostFunction {

  private static final ActivationFunction SIGMOID = ActivationFunctionSelector.SIGMOID
      .get();
  private final DenseDoubleMatrix x;
  private int[][] unfoldParameters;
  private DenseDoubleVector ones;

  public RBMCostFunction(DenseDoubleMatrix x, int numHiddenUnits) {
    this.ones = DenseDoubleVector.ones(x.getRowCount());
    this.x = new DenseDoubleMatrix(ones, x);
    this.unfoldParameters = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(new int[] { x.getColumnCount(), numHiddenUnits });
  }

  @Override
  public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {
    // input contains the weights between the visible and the hidden units
    DenseDoubleMatrix[] thetas = DenseMatrixFolder.unfoldMatrices(input,
        unfoldParameters);
    DenseDoubleMatrix thetaTransposed = thetas[0].transpose();
    DenseDoubleMatrix[] thetaGradients = new DenseDoubleMatrix[thetas.length];

    DoubleMatrix hiddenActivations = SIGMOID.apply(x.multiply(thetaTransposed));
    binarize(hiddenActivations);
    DoubleMatrix positiveAssociations = x.transpose().multiply(
        hiddenActivations);

    // start reconstructing the input
    DoubleMatrix fantasy = hiddenActivations.multiply(thetas[0]);
    DoubleMatrix hiddenFantasyActivations = SIGMOID.apply(fantasy
        .multiply(thetaTransposed));
    // set out fantasy bias back to 1
    fantasy.setColumnVector(0, ones);
    binarize(hiddenFantasyActivations);
    DoubleMatrix negativeAssociations = fantasy.transpose().multiply(
        hiddenFantasyActivations);

    double j = x.subtract(fantasy).pow(2).sum();

    thetaGradients[0] = (DenseDoubleMatrix) positiveAssociations
        .subtract(negativeAssociations).transpose().divide(x.getRowCount());

    return new Tuple<Double, DoubleVector>(j,
        DenseMatrixFolder.foldMatrices(thetaGradients));
  }

  private void binarize(DoubleMatrix hiddenActivations) {
    // set the states to binary 0 or 1
    final double rand = Math.random();
    for (int i = 0; i < hiddenActivations.getRowCount(); i++) {
      hiddenActivations.setRowVector(i, hiddenActivations.getRowVector(i)
          .apply(new DoubleVectorFunction() {
            @Override
            public double calculate(int index, double value) {
              return value > rand ? 1d : 0d;
            }
          }));
    }
  }
}
