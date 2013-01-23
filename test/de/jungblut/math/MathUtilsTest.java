package de.jungblut.math;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.math.DoubleMath;

import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.math.tuple.Tuple3;

public class MathUtilsTest extends TestCase {

  @Test
  public void testMeanNormalizeRows() {
    DoubleMatrix mat = new DenseDoubleMatrix(new double[][] { { 2, 5 },
        { 5, 1 }, { 7, 25 } });
    Tuple<DoubleMatrix, DoubleVector> normal = MathUtils.meanNormalizeRows(mat);
    DoubleVector mean = normal.getSecond();
    assertSmallDiff(mean, new DenseDoubleVector(new double[] { 3.5d, 3d, 16d }));
    DoubleMatrix meanNormalizedMatrix = normal.getFirst();
    DoubleMatrix matNormal = new DenseDoubleMatrix(new double[][] {
        { -1.5, 1.5 }, { 2, -2 }, { -9, 9 } });
    for (int i = 0; i < 3; i++) {
      assertSmallDiff(meanNormalizedMatrix.getRowVector(i),
          matNormal.getRowVector(i));
    }
  }

  @Test
  public void testFeatureNormalize() {
    DoubleMatrix mat = new DenseDoubleMatrix(new double[][] { { 2, 5 },
        { 5, 1 }, { 7, 25 } });
    Tuple3<DoubleMatrix, DoubleVector, DoubleVector> normal = MathUtils
        .meanNormalizeColumns(mat);
    DoubleVector mean = normal.getSecond();
    assertSmallDiff(mean, new DenseDoubleVector(new double[] { 14d / 3d,
        31d / 3d }));
    DoubleVector stddev = normal.getThird();
    assertSmallDiff(stddev, new DenseDoubleVector(new double[] { 2.0548046,
        10.498677 }));
    DoubleMatrix meanNormalizedMatrix = normal.getFirst();
    DoubleMatrix matNormal = new DenseDoubleMatrix(new double[][] {
        { -1.2977713, -0.508 }, { 0.162221421, -0.889 }, { 1.135549, 1.397 } });

    for (int i = 0; i < 3; i++) {
      assertSmallDiff(meanNormalizedMatrix.getRowVector(i),
          matNormal.getRowVector(i));
    }
  }

  @Test
  public void testPolynomials() {
    DenseDoubleMatrix mat = new DenseDoubleMatrix(new double[][] { { 2, 5 },
        { 5, 1 }, { 7, 25 } });
    DenseDoubleMatrix expected = new DenseDoubleMatrix(new double[][] {
        { 2, 4, 5, 25 }, { 5, 25, 1, 1 }, { 7, 49, 25, 625 } });
    DenseDoubleMatrix polys = MathUtils.createPolynomials(mat, 2);
    assertEquals(0,
        DoubleMath.fuzzyCompare(polys.subtract(expected).sum(), 0, 1E-5));
  }

  @Test
  public void testNumericalGradient() {
    // our function is f(x,y) = x^2+y^2
    // the derivative is f'(x,y) = 2x+2y
    final CostFunction inlineFunction = new CostFunction() {
      @Override
      public Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

        double cost = Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);
        DenseDoubleVector gradient = new DenseDoubleVector(new double[] {
            input.get(0) * 2, input.get(1) * 2 });

        return new Tuple<Double, DoubleVector>(cost, gradient);
      }
    };
    DenseDoubleVector v = new DenseDoubleVector(new double[] { 0, 1 });
    Tuple<Double, DoubleVector> cost = inlineFunction.evaluateCost(v);
    DoubleVector numericalGradient = MathUtils.numericalGradient(v,
        inlineFunction);
    assertSmallDiff(numericalGradient, cost.getSecond());

    v = new DenseDoubleVector(new double[] { -15, 100 });
    cost = inlineFunction.evaluateCost(v);

    numericalGradient = MathUtils.numericalGradient(v, inlineFunction);
    assertSmallDiff(numericalGradient, cost.getSecond());
  }

  private void assertSmallDiff(DoubleVector v1, DoubleVector v2) {
    assertEquals(v1.getLength(), v2.getLength());
    for (int i = 0; i < v2.getLength(); i++) {
      double d1 = v2.get(i);
      assertEquals(0, DoubleMath.fuzzyCompare(v1.get(i), d1, 1E-5));
    }
  }
}
