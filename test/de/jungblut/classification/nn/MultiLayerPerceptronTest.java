package de.jungblut.classification.nn;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public class MultiLayerPerceptronTest extends TestCase {

  @Test
  public void testXORNormalTrain() {

    MultilayerPerceptron mlp = new MultilayerPerceptron(new int[] { 2, 3, 1 });
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();

    mlp.train(sampleXOR.getFirst(), sampleXOR.getSecond(), 10000, 1.E-5, 0.1,
        0.0d, false);
    testPredictions(sampleXOR, mlp);
  }

  @Test
  public void testXORFminCG() {

    MultilayerPerceptron mlp = new MultilayerPerceptron(new int[] { 2, 4, 1 });
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();

    double error = mlp.trainFmincg(new DenseDoubleMatrix(sampleXOR.getFirst()),
        new DenseDoubleMatrix(sampleXOR.getSecond()), 30, 0.0d, false);
    assertTrue(error < 0.001);
    testPredictions(sampleXOR, mlp);
  }

  public void testPredictions(Tuple<DoubleVector[], DoubleVector[]> sampleXOR,
      MultilayerPerceptron mlp) {

    DoubleVector[] train = sampleXOR.getFirst();
    DoubleVector[] outcome = sampleXOR.getSecond();

    for (int i = 0; i < train.length; i++) {
      DenseDoubleVector predict = mlp.predict(train[i]);
      assertEquals(outcome[i].get(0), Math.rint(predict.get(0)));
    }

  }

  public Tuple<DoubleVector[], DoubleVector[]> sampleXOR() {
    DoubleVector[] train = new DoubleVector[] {
        new DenseDoubleVector(new double[] { 0, 0 }),
        new DenseDoubleVector(new double[] { 0, 1 }),
        new DenseDoubleVector(new double[] { 1, 0 }),
        new DenseDoubleVector(new double[] { 1, 1 }) };
    DoubleVector[] prediction = new DoubleVector[] {
        new DenseDoubleVector(new double[] { 0 }),
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 0 }) };
    return new Tuple<DoubleVector[], DoubleVector[]>(train, prediction);
  }

}
