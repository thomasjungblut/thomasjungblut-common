package de.jungblut.classification.nn;

import java.util.Random;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

public final class WeightMatrix {

  private final Layer leftLayer;
  private final Layer rightLayer;

  private DenseDoubleMatrix weights;
  private DenseDoubleMatrix derivatives;

  public WeightMatrix(Layer leftLayer, Layer rightLayer) {
    this.leftLayer = leftLayer;
    this.rightLayer = rightLayer;
    // extra row of weights for the bias unit, also random initialize them
    this.weights = new DenseDoubleMatrix(rightLayer.getLength(),
        leftLayer.getLength() + 1, new Random());
    this.derivatives = new DenseDoubleMatrix(weights.getRowCount(),
        weights.getColumnCount());
  }

  public WeightMatrix(Layer leftLayer, Layer rightLayer,
      DenseDoubleMatrix weights, DenseDoubleMatrix derivatives) {
    this.leftLayer = leftLayer;
    this.rightLayer = rightLayer;
    this.weights = weights;
    this.derivatives = derivatives;
  }

  public void updateWeights(int m, double learningRate, double lambda) {
    DoubleMatrix d = derivatives.divide(m).add(weights.multiply(lambda));
    weights = (DenseDoubleMatrix) weights.subtract(d.multiply(learningRate));
  }

  public void addDerivativesFromError() {
    DenseDoubleMatrix errors = rightLayer.getErrorsAsMatrix();
    // get the activations as transposed matrix (1xn)
    DenseDoubleMatrix activations = new DenseDoubleMatrix(
        leftLayer.getActivationsWithBias()).transpose();
    derivatives = (DenseDoubleMatrix) derivatives.add(errors
        .multiply(activations));
  }

  public void resetDerivatives() {
    for (int row = 0; row < weights.getRowCount(); row++) {
      for (int col = 0; col < weights.getColumnCount(); col++) {
        derivatives.set(row, col, 0.0d);
      }
    }
  }

  public void backwardError() {
    DenseDoubleVector activations = leftLayer.getActivationsWithBias();
    DoubleVector gPrime = activations.multiply(activations.multiply(-1.0).add(
        1.0));
    DenseDoubleVector rightErrors = rightLayer.getErrors();
    DoubleVector leftError = weights.transpose().multiplyVector(rightErrors)
        .multiply(gPrime);
    // slice the first item away because that's our bias unit
    leftLayer.setErrors(leftError.slice(1, leftError.getLength()));
  }

  public void forward() {
    rightLayer.setInputs(weights.multiplyVector(leftLayer
        .getActivationsWithBias()));
  }

  public DenseDoubleMatrix getWeights() {
    return weights;
  }

  public DenseDoubleMatrix getDerivatives() {
    return derivatives;
  }

  @Override
  public String toString() {
    return weights.toString();
  }
}
