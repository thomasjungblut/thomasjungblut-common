package de.jungblut.classification.regression;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.function.DoubleVectorFunction;
import de.jungblut.math.tuple.Tuple;

public class SparseMultiLabelRegressionTest {

  @Test
  public void testTraining() {

    ArrayList<Tuple<DoubleVector, DoubleVector>> data = getData();
    SparseMultiLabelRegression reg = new SparseMultiLabelRegression(5, 0.1, 5,
        5);
    reg.train(data);

    double loss = 0d;
    for (Tuple<DoubleVector, DoubleVector> dx : data) {
      DoubleVector prediction = reg.predict(dx.getFirst());
      prediction = new DenseDoubleVector(
          prediction.apply(new DoubleVectorFunction() {

            @Override
            public double calculate(int index, double value) {
              return value > 0.5 ? 1d : 0d;
            }
          }));
      loss += dx.getSecond().subtract(prediction).abs().sum();
    }

    assertEquals(1, loss, 1e-5);
  }

  @Test
  public void testRegularizedTraining() {

    ArrayList<Tuple<DoubleVector, DoubleVector>> data = getData();
    SparseMultiLabelRegression reg = new SparseMultiLabelRegression(5, 0.1, 5,
        5).setLambda(0.8);
    reg.train(data);

    double loss = 0d;
    for (Tuple<DoubleVector, DoubleVector> dx : data) {
      DoubleVector prediction = reg.predict(dx.getFirst());
      prediction = new DenseDoubleVector(
          prediction.apply(new DoubleVectorFunction() {

            @Override
            public double calculate(int index, double value) {
              return value > 0.5 ? 1d : 0d;
            }
          }));
      loss += dx.getSecond().subtract(prediction).abs().sum();
    }

    assertEquals(1, loss, 1e-5);
  }

  public ArrayList<Tuple<DoubleVector, DoubleVector>> getData() {
    ArrayList<Tuple<DoubleVector, DoubleVector>> stream = new ArrayList<>();
    // that is an extremely simple linear testcase
    stream.add(new Tuple<DoubleVector, DoubleVector>(new DenseDoubleVector(
        new double[] { 1d, 0d, 0d, 0d, 0d }), new DenseDoubleVector(
        new double[] { 1d, 1d, 0d, 0d, 0d })));
    stream.add(new Tuple<DoubleVector, DoubleVector>(new DenseDoubleVector(
        new double[] { 0d, 1d, 0d, 0d, 0d }), new DenseDoubleVector(
        new double[] { 1d, 0d, 0d, 0d, 1d })));
    stream.add(new Tuple<DoubleVector, DoubleVector>(new DenseDoubleVector(
        new double[] { 0d, 0d, 1d, 0d, 0d }), new DenseDoubleVector(
        new double[] { 0d, 0d, 1d, 0d, 0d })));
    stream.add(new Tuple<DoubleVector, DoubleVector>(new DenseDoubleVector(
        new double[] { 0d, 0d, 1d, 1d, 0d }), new DenseDoubleVector(
        new double[] { 0d, 0d, 1d, 1d, 0d })));
    return stream;
  }

}
