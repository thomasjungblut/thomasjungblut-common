package de.jungblut.classification.bayes;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.math.DoubleMath;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class MultinomialNaiveBayesClassifierTest extends TestCase {

  @Test
  public void testNaiveBayes() {

    MultinomialNaiveBayesClassifier classifier = new MultinomialNaiveBayesClassifier();

    DoubleVector[] features = new DoubleVector[] {
        new DenseDoubleVector(new double[] { 1, 0, 0, 0, 0 }),
        new DenseDoubleVector(new double[] { 1, 0, 0, 0, 0 }),
        new DenseDoubleVector(new double[] { 1, 1, 0, 0, 0 }),
        new DenseDoubleVector(new double[] { 0, 0, 1, 1, 1 }),
        new DenseDoubleVector(new double[] { 0, 0, 0, 1, 1 }), };
    DenseDoubleVector[] outcome = new DenseDoubleVector[] {
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 1 }),
        new DenseDoubleVector(new double[] { 0 }),
        new DenseDoubleVector(new double[] { 0 }), };
    classifier.train(features, outcome);

    DenseDoubleVector classProbability = classifier.getClassProbability();
    // we do add-one smoothing, so this may not sum to 1
    assertTrue(DoubleMath.fuzzyEquals(classProbability.get(0), 3d / 5d, 0.01d));
    assertTrue(DoubleMath.fuzzyEquals(classProbability.get(1), 4d / 5d, 0.01d));

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
      assertTrue(DoubleMath.fuzzyEquals(firstRow[i], realFirstRow[i], 0.05d));
    }

    double[] secondRow = mat.getRowVector(1).toArray();
    assertEquals(realSecondRow.length, secondRow.length);
    for (int i = 0; i < firstRow.length; i++) {
      assertTrue(DoubleMath.fuzzyEquals(secondRow[i], realSecondRow[i], 0.05d));
    }

    DoubleVector claz = classifier.predict(new DenseDoubleVector(new double[] {
        1, 0, 0, 0, 0 }));
    assertTrue("" + claz, DoubleMath.fuzzyEquals(claz.get(0), 0.14, 0.05d));
    assertTrue("" + claz, DoubleMath.fuzzyEquals(claz.get(1), 0.85, 0.05d));

    claz = classifier.predict(new DenseDoubleVector(new double[] { 0, 0, 0, 1,
        1 }));
    assertTrue("" + claz, DoubleMath.fuzzyEquals(claz.get(0), 0.85, 0.05d));
    assertTrue("" + claz, DoubleMath.fuzzyEquals(claz.get(1), 0.15, 0.05d));
  }

}
