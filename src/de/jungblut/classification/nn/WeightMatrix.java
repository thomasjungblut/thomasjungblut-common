package de.jungblut.classification.nn;

import org.apache.commons.math3.random.RandomDataImpl;

import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Weight matrix wrapper to encapsulate the random initialization.
 * 
 * @author thomas.jungblut
 * 
 */
public final class WeightMatrix {

  private DenseDoubleMatrix weights;

  /**
   * Creates a [unitsRightLayer x (unitsLeftLayer + 1)] matrix of weights and
   * seed the values using the famous uniform distribution formula of LeCun.
   * Which is calculating the deviation of the weights by SQRT(6)/((num units
   * left layer)+(num units right layer)) and distributing them with zero mean.
   */
  public WeightMatrix(int unitsLeftLayer, int unitsRightLayer) {
    this.weights = new DenseDoubleMatrix(unitsRightLayer, unitsLeftLayer + 1);
    double eInit = Math.sqrt(6) / Math.sqrt(unitsLeftLayer + unitsRightLayer);
    setWeightsUniformly(seedRandomGenerator(), eInit);
  }

  /**
   * Creates a [unitsRightLayer x (unitsLeftLayer + 1)] matrix of weights and
   * seed the values like described in
   * "A Practical Guide to Training Restricted Boltzmann Machines" by Geoffrey
   * Hinton. Which distributing random weights with 0.01 standard deviation and
   * zero mean and setting the hidden bias to 0 (this is a parameter here).
   */
  public WeightMatrix(int unitsLeftLayer, int unitsRightLayer, double biasValue) {
    this.weights = new DenseDoubleMatrix(unitsRightLayer, unitsLeftLayer + 1);
    setWeightsUniformly(seedRandomGenerator(), 0.01d);
    this.weights.setColumnVector(0, new DenseDoubleVector(unitsRightLayer,
        biasValue));
  }

  /**
   * Sets the weights in the whole matrix uniformly between -eInit and eInit
   * (eInit is the standard deviation) with zero mean.
   */
  private void setWeightsUniformly(RandomDataImpl rnd, double eInit) {
    for (int i = 0; i < weights.getColumnCount(); i++) {
      for (int j = 0; j < weights.getRowCount(); j++) {
        weights.set(j, i, rnd.nextUniform(-eInit, eInit));
      }
    }
  }

  private RandomDataImpl seedRandomGenerator() {
    RandomDataImpl rnd = new RandomDataImpl();
    rnd.reSeed(MultilayerPerceptron.SEED);
    rnd.reSeedSecure(MultilayerPerceptron.SEED);
    return rnd;
  }

  public WeightMatrix(DenseDoubleMatrix weights) {
    this.weights = weights;
  }

  public DenseDoubleMatrix getWeights() {
    return weights;
  }

  public void setWeights(DenseDoubleMatrix weights) {
    this.weights = weights;
  }

  @Override
  public String toString() {
    return weights.toString();
  }
}
