package de.jungblut.classification.nn;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

public final class Layer {

  private final DenseDoubleVector activations;
  private final DenseDoubleVector errors;
  private final int length;

  public Layer(int neuronCount) {
    this.length = neuronCount;
    activations = new DenseDoubleVector(neuronCount);
    errors = new DenseDoubleVector(neuronCount);
  }

  public Layer(int neuronCount, DenseDoubleVector activations,
      DenseDoubleVector errors) {
    this.length = neuronCount;
    this.activations = activations;
    this.errors = errors;
  }

  public DenseDoubleVector getActivations() {
    return activations;
  }

  public DenseDoubleVector getActivationsWithBias() {
    DenseDoubleVector v = new DenseDoubleVector(activations.getLength() + 1);
    v.set(0, 1.0d); // bias unit is always at index zero
    for (int i = 0; i < activations.getLength(); i++) {
      v.set(i + 1, activations.get(i));
    }
    return v;
  }

  public DenseDoubleVector getErrors() {
    return errors;
  }

  public DenseDoubleMatrix getErrorsAsMatrix() {
    return new DenseDoubleMatrix(errors);
  }

  public void setActivations(DoubleVector a) {
    for (int i = 0; i < a.getLength(); i++) {
      activations.set(i, a.get(i));
    }
  }

  public void setErrors(DoubleVector a) {
    for (int i = 0; i < a.getLength(); i++) {
      errors.set(i, a.get(i));
    }
  }

  public int getLength() {
    return length;
  }

  public void setInputs(DoubleVector in) {
    for (int i = 0; i < in.getLength(); i++) {
      activations.set(i, sigmoid(in.get(i)));
    }
  }

  private static double sigmoid(double input) {
    return 1.0 / (1.0 + Math.exp(-input));
  }

  @Override
  public String toString() {
    return activations.toString();
  }
}
