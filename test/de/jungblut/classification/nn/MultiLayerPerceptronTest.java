package de.jungblut.classification.nn;

import static de.jungblut.math.activation.ActivationFunctionSelector.LINEAR;
import static de.jungblut.math.activation.ActivationFunctionSelector.SIGMOID;
import static de.jungblut.math.activation.ActivationFunctionSelector.SOFTMAX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.activation.ActivationFunctionSelector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.minimize.GradientDescent;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.math.minimize.ParticleSwarmOptimization;
import de.jungblut.math.squashing.CrossEntropyErrorFunction;
import de.jungblut.math.squashing.LogisticErrorFunction;
import de.jungblut.math.squashing.SquaredMeanErrorFunction;
import de.jungblut.math.tuple.Tuple;

public class MultiLayerPerceptronTest {

  static {
    MultilayerPerceptron.SEED = 0L;
  }

  @Test
  public void testParableRegression() {
    // sample a parable of points and use one hidden layer
    MultilayerPerceptron mlp = MultilayerPerceptron.MultilayerPerceptronBuilder
        .create(
            new int[] { 2, 2, 1 },
            new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                LINEAR.get() }, new SquaredMeanErrorFunction(), new Fmincg(),
            10000).verbose(false).build();

    // sample a parable of points
    Tuple<DoubleVector[], DoubleVector[]> sample = sampleParable();

    mlp.train(sample.getFirst(), sample.getSecond());
    double diff = validateRegressionPredictions(sample, mlp);
    // this actually takes some time to converge properly, so we just test for
    // 10k epochs with a loose threshold.
    assertEquals(63000d, diff, 1000d);
    System.out.println(diff);
  }

  @Test
  public void testRegression() {
    // test the linear regression case
    // use a gradient descent with very small learning rate
    MultilayerPerceptron mlp = MultilayerPerceptron.MultilayerPerceptronBuilder
        .create(new int[] { 2, 1 },
            new ActivationFunction[] { LINEAR.get(), LINEAR.get() },
            new SquaredMeanErrorFunction(), new GradientDescent(1e-8, 6e-5),
            10000).verbose(false).build();

    // sample a line of points
    Tuple<DoubleVector[], DoubleVector[]> sample = sampleLinear();

    mlp.train(sample.getFirst(), sample.getSecond());
    double diff = validateRegressionPredictions(sample, mlp);
    assertEquals(diff, 2449d, 10d);
  }

  @Test
  public void testXORSoftMaxFminCG() {
    MultilayerPerceptron mlp = MultilayerPerceptron.MultilayerPerceptronBuilder
        .create(
            new int[] { 2, 4, 2 },
            new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                SOFTMAX.get() }, new CrossEntropyErrorFunction(), new Fmincg(),
            100).build();
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXORSoftMax();
    double error = mlp.train(sampleXOR.getFirst(), sampleXOR.getSecond(),
        new Fmincg(), 100, 0.0d, false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.01);
      testPredictionsSoftMax(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
  }

  @Test
  public void testXORElliotFminCG() {
    MultilayerPerceptron mlp = MultilayerPerceptron.MultilayerPerceptronBuilder
        .create(
            new int[] { 2, 4, 1 },
            new ActivationFunction[] { LINEAR.get(),
                ActivationFunctionSelector.ELLIOT.get(),
                ActivationFunctionSelector.ELLIOT.get() },
            new LogisticErrorFunction(), new Fmincg(), 100).build();
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();
    double error = mlp.train(sampleXOR.getFirst(), sampleXOR.getSecond(),
        new Fmincg(), 100, 0.0d, false);
    System.out.println(error);
    // increase the error here a bit, because it is just an approx. to sigmoid
    if (error < 0.02) {
      assertTrue(error < 0.02);
      testPredictionsSoftMax(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
  }

  @Test
  public void testXORFminCG() {
    MultilayerPerceptron mlp = MultilayerPerceptron.MultilayerPerceptronBuilder
        .create(
            new int[] { 2, 4, 1 },
            new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                SIGMOID.get() }, new LogisticErrorFunction(), new Fmincg(), 100)
        .build();
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();
    double error = mlp.train(sampleXOR.getFirst(), sampleXOR.getSecond(),
        new Fmincg(), 100, 0.0d, false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.001);
      validatePredictions(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
  }

  @Test
  public void testXORPSO() {
    MultilayerPerceptron mlp = MultilayerPerceptron.MultilayerPerceptronBuilder
        .create(
            new int[] { 2, 4, 1 },
            new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                SIGMOID.get() }, new LogisticErrorFunction(), new Fmincg(), 100)
        .build();
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();
    double error = mlp.train(sampleXOR.getFirst(), sampleXOR.getSecond(),
        new ParticleSwarmOptimization(1000, 2.8d, 0.2, 0.4, 4), 400, 0.0d,
        false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.001);
      validatePredictions(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
  }

  @SuppressWarnings("resource")
  @Test
  public void testSerialization() throws Exception {
    MultilayerPerceptron mlp = validateXorSigmoidNetwork(null);
    File tmp = File.createTempFile("neuraltest", ".tmp");
    DataOutputStream out = new DataOutputStream(new FileOutputStream(tmp));
    MultilayerPerceptron.serialize(mlp, out);
    out.close();
    DataInputStream in = new DataInputStream(new FileInputStream(tmp));
    validateXorSigmoidNetwork(MultilayerPerceptron.deserialize(in));
    in.close();
  }

  @Test
  public void testStochasticLearning() throws Exception {
    Minimizer minimizer = GradientDescent.GradientDescentBuilder.create(0.05)
        .build();
    MultilayerPerceptron mlp = MultilayerPerceptron.MultilayerPerceptronBuilder
        .create(
            new int[] { 2, 4, 1 },
            new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                SIGMOID.get() }, new LogisticErrorFunction(), minimizer, 15000)
        .stochastic().miniBatchSize(1).build();
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();
    mlp.train(sampleXOR.getFirst(), sampleXOR.getSecond());
    validatePredictions(sampleXOR, mlp);
  }

  private MultilayerPerceptron validateXorSigmoidNetwork(
      MultilayerPerceptron mlp) {
    if (mlp == null) {
      mlp = MultilayerPerceptron.MultilayerPerceptronBuilder
          .create(
              new int[] { 2, 4, 1 },
              new ActivationFunction[] { LINEAR.get(), SIGMOID.get(),
                  SIGMOID.get() }, new LogisticErrorFunction(), new Fmincg(),
              100).build();
    }
    Tuple<DoubleVector[], DoubleVector[]> sampleXOR = sampleXOR();
    double error = mlp.train(sampleXOR.getFirst(), sampleXOR.getSecond(),
        new Fmincg(), 100, 0.0d, false);
    System.out.println(error);
    if (error < 0.01) {
      assertTrue(error < 0.001);
      validatePredictions(sampleXOR, mlp);
    } else {
      throw new RuntimeException("Test seems flaky..");
    }
    return mlp;
  }

  public void validatePredictions(
      Tuple<DoubleVector[], DoubleVector[]> sampleXOR, MultilayerPerceptron mlp) {

    DoubleVector[] train = sampleXOR.getFirst();
    DoubleVector[] outcome = sampleXOR.getSecond();

    for (int i = 0; i < train.length; i++) {
      DoubleVector predict = mlp.predict(train[i]);
      assertEquals(outcome[i].get(0), Math.rint(predict.get(0)), 1e-4);
    }
  }

  public double validateRegressionPredictions(
      Tuple<DoubleVector[], DoubleVector[]> sampleXOR, MultilayerPerceptron mlp) {

    DoubleVector[] train = sampleXOR.getFirst();
    DoubleVector[] outcome = sampleXOR.getSecond();

    double absDifference = 0d;
    for (int i = 0; i < train.length; i++) {
      DoubleVector predict = mlp.predict(train[i]);
      absDifference += Math.abs(outcome[i].get(0) - predict.get(0));
    }
    return absDifference;
  }

  private Tuple<DoubleVector[], DoubleVector[]> sampleLinear() {
    // sample some points from 0 to 2000
    DoubleVector[] train = new DoubleVector[2000];
    DoubleVector[] outcome = new DoubleVector[2000];

    for (int i = 0; i < train.length; i++) {
      train[i] = new DenseDoubleVector(new double[] { i, i });
      outcome[i] = new DenseDoubleVector(new double[] { i });
    }

    return new Tuple<>(train, outcome);
  }

  private Tuple<DoubleVector[], DoubleVector[]> sampleParable() {
    // sample some points from 0 to 100
    DoubleVector[] train = new DoubleVector[100];
    DoubleVector[] outcome = new DoubleVector[100];

    for (int i = 0; i < train.length; i++) {
      train[i] = new DenseDoubleVector(new double[] { i, i });
      outcome[i] = new DenseDoubleVector(new double[] { i * i });
    }

    return new Tuple<>(train, outcome);
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
      DoubleVector predict = mlp.predict(train[i]);
      assertEquals(outcome[i].get(0), Math.rint(predict.get(0)), 1e-4);
    }
  }

}
