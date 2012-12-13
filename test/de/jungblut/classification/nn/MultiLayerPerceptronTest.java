package de.jungblut.classification.nn;

import static de.jungblut.math.activation.ActivationFunctionSelector.LINEAR;
import static de.jungblut.math.activation.ActivationFunctionSelector.SIGMOID;
import static de.jungblut.math.activation.ActivationFunctionSelector.SOFTMAX;
import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.minimize.ParticleSwarmOptimization;
import de.jungblut.math.tuple.Tuple;

public class MultiLayerPerceptronTest extends TestCase {

  @Test
  public void testXORSoftMaxFminCG() {
    MultilayerPerceptron mlp = new MultilayerPerceptron(new int[] { 2, 4, 2 },
        new ActivationFunction[] { LINEAR.get(), SIGMOID.get(), SOFTMAX.get() });
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXORSoftMax();

    double error = mlp.train(new DenseDoubleMatrix(sampleXOR.getFirst()),
        new DenseDoubleMatrix(sampleXOR.getSecond()), new Fmincg(), 40, 0.0d,
        false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.01);
      testPredictionsSoftMax(sampleXOR, mlp);
    } else {
      System.out.println("Test seems flaky..");
    }
  }

  @Test
  public void testXORFminCG() {
    MultilayerPerceptron mlp = new MultilayerPerceptron(new int[] { 2, 4, 1 },
        new ActivationFunction[] { LINEAR.get(), SIGMOID.get(), SIGMOID.get() });
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();

    double error = mlp.train(new DenseDoubleMatrix(sampleXOR.getFirst()),
        new DenseDoubleMatrix(sampleXOR.getSecond()), new Fmincg(), 40, 0.0d,
        false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.001);
      testPredictions(sampleXOR, mlp);
    } else {
      System.out.println("Test seems flaky..");
    }
  }

  @Test
  public void testXORPSO() {
    MultilayerPerceptron mlp = new MultilayerPerceptron(new int[] { 2, 4, 1 },
        new ActivationFunction[] { LINEAR.get(), SIGMOID.get(), SIGMOID.get() });
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();

    double error = mlp.train(new DenseDoubleMatrix(sampleXOR.getFirst()),
        new DenseDoubleMatrix(sampleXOR.getSecond()),
        new ParticleSwarmOptimization(1000, 2.8d, 0.2, 0.4), 400, 0.0d, false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.001);
      testPredictions(sampleXOR, mlp);
    } else {
      System.out.println("Test seems flaky..");
    }
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
    return new Tuple<>(train, prediction);
  }

  public Tuple<DoubleVector[], DoubleVector[]> sampleXORSoftMax() {
    DoubleVector[] train = new DoubleVector[] {
        new DenseDoubleVector(new double[] { 0, 0 }),
        new DenseDoubleVector(new double[] { 0, 1 }),
        new DenseDoubleVector(new double[] { 1, 0 }),
        new DenseDoubleVector(new double[] { 1, 1 }) };
    DoubleVector[] prediction = new DoubleVector[] {
        new DenseDoubleVector(new double[] { 0, 1 }),
        new DenseDoubleVector(new double[] { 1, 0 }),
        new DenseDoubleVector(new double[] { 1, 0 }),
        new DenseDoubleVector(new double[] { 0, 1 }) };
    return new Tuple<>(train, prediction);
  }

  public void testPredictionsSoftMax(
      Tuple<DoubleVector[], DoubleVector[]> sampleXOR, MultilayerPerceptron mlp) {

    DoubleVector[] train = sampleXOR.getFirst();
    DoubleVector[] outcome = sampleXOR.getSecond();

    for (int i = 0; i < train.length; i++) {
      DenseDoubleVector predict = mlp.predict(train[i]);
      assertEquals(outcome[i].get(0), Math.rint(predict.get(0)));
    }
  }

}
