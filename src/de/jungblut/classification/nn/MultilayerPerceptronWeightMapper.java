package de.jungblut.classification.nn;

import de.jungblut.classification.ClassifierFactory;
import de.jungblut.classification.eval.WeightMapper;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.minimize.DenseMatrixFolder;

public class MultilayerPerceptronWeightMapper implements
    WeightMapper<MultilayerPerceptron> {

  private final int[][] unfoldParameters;
  private final MultilayerPerceptron classifier;

  public MultilayerPerceptronWeightMapper(
      ClassifierFactory<MultilayerPerceptron> factory) {
    this.classifier = factory.newInstance();
    unfoldParameters = MultilayerPerceptronCostFunction
        .computeUnfoldParameters(classifier.getLayers());
  }

  @Override
  public MultilayerPerceptron mapWeights(DoubleVector weights) {
    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        weights, unfoldParameters);
    for (int i = 0; i < unfoldMatrices.length; i++) {
      classifier.getWeights()[i].setWeights(unfoldMatrices[i]);
    }
    return classifier;
  }

}
