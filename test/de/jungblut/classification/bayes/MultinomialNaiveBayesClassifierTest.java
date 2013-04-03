package de.jungblut.classification.bayes;

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

public class MultinomialNaiveBayesClassifierTest extends TestCase {

  @Test
  public void testNaiveBayes() {

    MultinomialNaiveBayesClassifier classifier = new MultinomialNaiveBayesClassifier();

    DoubleVector[] features = new DoubleVector[] {
        new SparseDoubleVector(new double[] { 1, 0, 0, 0, 0 }),
        new SparseDoubleVector(new double[] { 1, 0, 0, 0, 0 }),
        new SparseDoubleVector(new double[] { 1, 1, 0, 0, 0 }),
        new SparseDoubleVector(new double[] { 0, 0, 1, 1, 1 }),
        new SparseDoubleVector(new double[] { 0, 0, 0, 1, 1 }), };
    DenseDoubleVector[] outcome = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 0 }),
        new DenseDoubleVector(new double[] { 0 }), };
    classifier.train(features, outcome);

    DenseDoubleVector classProbability = classifier.getClassProbability();
    assertEquals(classProbability.get(0), 2d / 5d, 0.01d);
    assertEquals(classProbability.get(1), 3d / 5d, 0.01d);

    DoubleMatrix mat = classifier.getProbabilityMatrix();
    double[] realFirstRow = new double[] { -2.1972245773362196,
        -2.1972245773362196, -1.5040773967762742, -1.0986122886681098,
        -1.0986122886681098 };
    double[] realSecondRow = new double[] { -0.6931471805599453,
        -1.3862943611198906, -2.0794415416798357, -2.0794415416798357,
        -2.0794415416798357 };

    double[] firstRow = mat.getRowVector(0).toArray();
    assertEquals(realFirstRow.length, firstRow.length);
    for (int i = 0; i < firstRow.length; i++) {
      assertEquals("" + Arrays.toString(firstRow), firstRow[i],
          realFirstRow[i], 0.05d);
    }

    double[] secondRow = mat.getRowVector(1).toArray();
    assertEquals(realSecondRow.length, secondRow.length);
    for (int i = 0; i < firstRow.length; i++) {
      assertEquals("" + Arrays.toString(secondRow), secondRow[i],
          realSecondRow[i], 0.05d);
    }

    DoubleVector claz = classifier.predict(new DenseDoubleVector(new double[] {
        1, 0, 0, 0, 0 }));
    assertEquals("" + claz, claz.get(0), 0.14, 0.05d);
    assertEquals("" + claz, claz.get(1), 0.85, 0.05d);

    claz = classifier.predict(new DenseDoubleVector(new double[] { 0, 0, 0, 1,
        1 }));
    assertEquals("" + claz, claz.get(0), 0.85, 0.05d);
    assertEquals("" + claz, claz.get(1), 0.15, 0.05d);
  }

}
