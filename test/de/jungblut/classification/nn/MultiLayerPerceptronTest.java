package de.jungblut.classification.nn;

import static de.jungblut.math.activation.ActivationFunctionSelector.LINEAR;
import static de.jungblut.math.activation.ActivationFunctionSelector.SIGMOID;
import static de.jungblut.math.activation.ActivationFunctionSelector.SOFTMAX;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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

  static {
    MultilayerPerceptron.SEED = 0L;
  }

  @Test
  public void testXORSoftMaxFminCG() {
    MultilayerPerceptron mlp = MultilayerPerceptron.TrainingConfiguration
        .newConfiguration(
            new int[] { 2, 4, 2 },
            new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                SOFTMAX.get() }, new Fmincg(), 100).build();

    Tuple<DoubleVector[], DenseDoubleVector[]> sampleXOR = sampleXORSoftMax();

    double error = mlp.train(new DenseDoubleMatrix(sampleXOR.getFirst()),
        new DenseDoubleMatrix(sampleXOR.getSecond()), new Fmincg(), 100, 0.0d,
        false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.01);
      testPredictionsSoftMax(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
  }

  @Test
  public void testXORFminCG() {
    MultilayerPerceptron mlp = MultilayerPerceptron.TrainingConfiguration
        .newConfiguration(
            new int[] { 2, 4, 1 },
            new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                SIGMOID.get() }, new Fmincg(), 100).build();
    Tuple<DoubleVector[], DenseDoubleVector[]> sampleXOR = sampleXOR();

    double error = mlp.train(new DenseDoubleMatrix(sampleXOR.getFirst()),
        new DenseDoubleMatrix(sampleXOR.getSecond()), new Fmincg(), 100, 0.0d,
        false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.001);
      testPredictions(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
  }

  @Test
  public void testXORPSO() {
    MultilayerPerceptron mlp = MultilayerPerceptron.TrainingConfiguration
        .newConfiguration(
            new int[] { 2, 4, 1 },
            new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                SIGMOID.get() }, new Fmincg(), 100).build();
    Tuple<DoubleVector[], DenseDoubleVector[]> sampleXOR = sampleXOR();

    double error = mlp.train(new DenseDoubleMatrix(sampleXOR.getFirst()),
        new DenseDoubleMatrix(sampleXOR.getSecond()),
        new ParticleSwarmOptimization(1000, 2.8d, 0.2, 0.4), 400, 0.0d, false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.001);
      testPredictions(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
  }

  @Test
  public void testSerialization() throws Exception {
    MultilayerPerceptron mlp = testXorSigmoidNetwork(null);
    File tmp = File.createTempFile("neuraltest", ".tmp");
    DataOutputStream out = new DataOutputStream(new FileOutputStream(tmp));
    MultilayerPerceptron.serialize(mlp, out);
    out.close();

    DataInputStream in = new DataInputStream(new FileInputStream(tmp));
    testXorSigmoidNetwork(MultilayerPerceptron.deserialize(in));
    in.close();

  }

  private MultilayerPerceptron testXorSigmoidNetwork(MultilayerPerceptron mlp) {
    if (mlp == null) {
      mlp = MultilayerPerceptron.TrainingConfiguration
          .newConfiguration(
              new int[] { 2, 4, 1 },
              new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                  SIGMOID.get() }, new Fmincg(), 100).build();
    }
    Tuple<DoubleVector[], DenseDoubleVector[]> sampleXOR = sampleXOR();

    double error = mlp.train(new DenseDoubleMatrix(sampleXOR.getFirst()),
        new DenseDoubleMatrix(sampleXOR.getSecond()), new Fmincg(), 100, 0.0d,
        false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.001);
      testPredictions(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
    return mlp;
  }

  public void testPredictions(
      Tuple<DoubleVector[], DenseDoubleVector[]> sampleXOR,
      MultilayerPerceptron mlp) {

    DoubleVector[] train = sampleXOR.getFirst();
    DoubleVector[] outcome = sampleXOR.getSecond();

    for (int i = 0; i < train.length; i++) {
      DenseDoubleVector predict = mlp.predict(train[i]);
      assertEquals(outcome[i].get(0), Math.rint(predict.get(0)));
    }
  }

  public Tuple<DoubleVector[], DenseDoubleVector[]> sampleXOR() {
    DoubleVector[] train = new DoubleVector[] {
        new DenseDoubleVector(new double[] { 0, 0 }),
        new DenseDoubleVector(new double[] { 0, 1 }),
        new DenseDoubleVector(new double[] { 1, 0 }),
        new DenseDoubleVector(new double[] { 1, 1 }) };
    DenseDoubleVector[] prediction = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 0 }),
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 0 }) };
    return new Tuple<>(train, prediction);
  }

  public Tuple<DoubleVector[], DenseDoubleVector[]> sampleXORSoftMax() {
    DoubleVector[] train = new DoubleVector[] {
        new DenseDoubleVector(new double[] { 0, 0 }),
        new DenseDoubleVector(new double[] { 0, 1 }),
        new DenseDoubleVector(new double[] { 1, 0 }),
        new DenseDoubleVector(new double[] { 1, 1 }) };
    DenseDoubleVector[] prediction = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 0, 1 }),
        new DenseDoubleVector(new double[] { 1, 0 }),
        new DenseDoubleVector(new double[] { 1, 0 }),
        new DenseDoubleVector(new double[] { 0, 1 }) };
    return new Tuple<>(train, prediction);
  }

  public void testPredictionsSoftMax(
      Tuple<DoubleVector[], DenseDoubleVector[]> sampleXOR,
      MultilayerPerceptron mlp) {

    DoubleVector[] train = sampleXOR.getFirst();
    DoubleVector[] outcome = sampleXOR.getSecond();

    for (int i = 0; i < train.length; i++) {
      DenseDoubleVector predict = mlp.predict(train[i]);
      assertEquals(outcome[i].get(0), Math.rint(predict.get(0)));
    }
  }

}
