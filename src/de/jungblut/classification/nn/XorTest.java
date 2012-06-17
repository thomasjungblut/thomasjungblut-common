package de.jungblut.classification.nn;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

public final class XorTest {

  public static Tuple<DoubleVector[], DoubleVector[]> getXORTraining() {
    DoubleVector[] xor = new DoubleVector[4];
    DoubleVector[] xorOutcome = new DoubleVector[4];
    xor[0] = new DenseDoubleVector(new double[] { 0, 0 });
    xorOutcome[0] = new DenseDoubleVector(new double[] { 0 });
    xor[1] = new DenseDoubleVector(new double[] { 1, 0 });
    xorOutcome[1] = new DenseDoubleVector(new double[] { 1 });
    xor[2] = new DenseDoubleVector(new double[] { 0, 1 });
    xorOutcome[2] = new DenseDoubleVector(new double[] { 1 });
    xor[3] = new DenseDoubleVector(new double[] { 1, 1 });
    xorOutcome[3] = new DenseDoubleVector(new double[] { 0 });
    return new Tuple<DoubleVector[], DoubleVector[]>(xor, xorOutcome);
  }

  public static void checkOutcome(
      Tuple<DoubleVector[], DoubleVector[]> xorTraining, MultilayerPerceptron nn) {
    DoubleVector[] xor = xorTraining.getFirst();
    DoubleVector[] xorOutcome = xorTraining.getSecond();
    for (int i = 0; i < xor.length; i++) {
      DenseDoubleVector predict = nn.predict(xor[i]);
      if (xorOutcome[i].get(0) > 0.5 ? predict.get(0) > 0.5
          : predict.get(0) <= 0.5) {
        System.out.println("For " + xor[i] + " " + predict);
      } else {
        throw new RuntimeException("Fail!");
      }
    }
  }

  public static void testManualTraining() {
    Tuple<DoubleVector[], DoubleVector[]> xorTraining = getXORTraining();
    MultilayerPerceptron trainManual = trainManual(xorTraining.getFirst(),
        xorTraining.getSecond());
    checkOutcome(xorTraining, trainManual);
  }

  public static void testAutomaticTraining() {
    Tuple<DoubleVector[], DoubleVector[]> xorTraining = getXORTraining();
    MultilayerPerceptron trainManual = trainAuto(xorTraining.getFirst(),
        xorTraining.getSecond());
    checkOutcome(xorTraining, trainManual);
  }

  private static MultilayerPerceptron trainAuto(DoubleVector[] xor,
      DoubleVector[] xorOutcome) {
    MultilayerPerceptron nn = new MultilayerPerceptron(new int[] { 2, 3, 1 });
    nn.train(xor, xorOutcome, 50000, 0.001, 0.1d, 2.0d, false);
    return nn;
  }

  private static MultilayerPerceptron trainManual(DoubleVector[] xor,
      DoubleVector[] xorOutcome) {
    MultilayerPerceptron nn = new MultilayerPerceptron(new int[] { 2, 3, 1 });

    for (int iteration = 0; iteration < 1000; iteration++) {
      nn.resetGradients();
      for (int i = 0; i < xor.length; i++) {
        DoubleVector difference = nn.forwardStep(xor[i], xorOutcome[i]);
        nn.backwardStep(difference);
      }
      nn.adjustWeights(xor.length, 1.0, 0.0d);
    }
    return nn;
  }

}
