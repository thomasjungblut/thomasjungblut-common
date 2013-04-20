package de.jungblut.classification.nn;

import org.apache.commons.math3.random.RandomDataImpl;

import de.jungblut.math.dense.DenseDoubleMatrix;

/**
 * Weight matrix wrapper to encapsulate the random initialization.
 * 
 * @author thomas.jungblut
 * 
 */
public final class WeightMatrix {

  private DenseDoubleMatrix weights;

  /**
   * Creates a unitsRightLayer x (unitsLeftLayer+1) matrix of weights and seed
   * the values using the famous uniform distribution formula of LeCun.
   */
  public WeightMatrix(int unitsLeftLayer, int unitsRightLayer) {
    // extra row of weights for the bias unit, also random initialize them
    this.weights = new DenseDoubleMatrix(unitsRightLayer, unitsLeftLayer + 1);
    double eInit = Math.sqrt(6) / Math.sqrt(unitsLeftLayer + unitsRightLayer);
    RandomDataImpl rnd = new RandomDataImpl();
    rnd.reSeed(MultilayerPerceptron.SEED);
    rnd.reSeedSecure(MultilayerPerceptron.SEED);
    for (int i = 0; i < weights.getColumnCount(); i++) {
      for (int j = 0; j < weights.getRowCount(); j++) {
        weights.set(j, i, rnd.nextUniform(-eInit, eInit));
      }
    }
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
