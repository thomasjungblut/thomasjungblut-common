package de.jungblut.math;

import de.jungblut.math.MathUtils.PredictionOutcomePair;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.CostGradientTuple;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.math.tuple.Tuple3;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class MathUtilsTest {

    @Test
    public void testMeanNormalizeRows() {
        DoubleMatrix mat = new DenseDoubleMatrix(new double[][]{{2, 5},
                {5, 1}, {7, 25}});
        Tuple<DoubleMatrix, DoubleVector> normal = MathUtils.meanNormalizeRows(mat);
        DoubleVector mean = normal.getSecond();
        assertSmallDiff(mean, new DenseDoubleVector(new double[]{3.5d, 3d, 16d}));
        DoubleMatrix meanNormalizedMatrix = normal.getFirst();
        DoubleMatrix matNormal = new DenseDoubleMatrix(new double[][]{
                {-1.5, 1.5}, {2, -2}, {-9, 9}});
        for (int i = 0; i < 3; i++) {
            assertSmallDiff(meanNormalizedMatrix.getRowVector(i),
                    matNormal.getRowVector(i));
        }
    }

    @Test
    public void testMinMaxScale() {
        // just testing the core functionality
        double value = 25;
        double result = MathUtils.minMaxScale(value, 0, 100, 0, 1);
        assertEquals(0.25, result, 1e-5);

        DoubleVector in = new DenseDoubleVector(new double[]{40, 60});
        DoubleVector res = new DenseDoubleVector(new double[]{0.4, 0.6});

        DoubleVector minMaxScale = MathUtils.minMaxScale(in, 0, 100, 0, 1);
        assertSmallDiff(res, minMaxScale);

        DoubleMatrix mat = new DenseDoubleMatrix(new double[][]{{40, 60},
                {2, 25}});
        DoubleMatrix resMat = new DenseDoubleMatrix(new double[][]{{0.4, 0.6},
                {0.02, 0.25}});

        DoubleMatrix minMaxScaleMat = MathUtils.minMaxScale(mat, 0, 100, 0, 1);
        assertSmallDiff(resMat, minMaxScaleMat);

    }

    @Test
    public void testLogMatrix() {
        DoubleMatrix y = new DenseDoubleMatrix(
                new double[][]{{0d, 1d, 0.5d, Double.NaN, 0.2d,
                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}});
        DoubleMatrix mat = MathUtils.logMatrix(y);
        assertEquals(-10d, mat.get(0, 0), 1e-4);
        assertEquals(0d, mat.get(0, 1), 1e-4);
        assertEquals(-0.6931471805599453, mat.get(0, 2), 1e-4);
        assertEquals(0d, mat.get(0, 3), 1e-4);
        assertEquals(-1.6094379124341003, mat.get(0, 4), 1e-4);
        assertEquals(0d, mat.get(0, 5), 1e-4);
        assertEquals(0d, mat.get(0, 6), 1e-4);
    }

    @Test
    public void testFeatureNormalize() {
        DoubleMatrix mat = new DenseDoubleMatrix(new double[][]{{2, 5},
                {5, 1}, {7, 25}});
        Tuple3<DoubleMatrix, DoubleVector, DoubleVector> normal = MathUtils
                .meanNormalizeColumns(mat);
        DoubleVector mean = normal.getSecond();
        assertSmallDiff(mean, new DenseDoubleVector(new double[]{14d / 3d,
                31d / 3d}));
        DoubleVector stddev = normal.getThird();
        assertSmallDiff(stddev, new DenseDoubleVector(new double[]{2.0548046,
                10.498677}));
        DoubleMatrix meanNormalizedMatrix = normal.getFirst();
        DoubleMatrix matNormal = new DenseDoubleMatrix(new double[][]{
                {-1.2977713, -0.508}, {0.162221421, -0.889}, {1.135549, 1.397}});

        for (int i = 0; i < 3; i++) {
            assertSmallDiff(meanNormalizedMatrix.getRowVector(i),
                    matNormal.getRowVector(i));
        }
    }

    @Test
    public void testPolynomials() {
        DenseDoubleMatrix mat = new DenseDoubleMatrix(new double[][]{{2, 5},
                {5, 1}, {7, 25}});
        DenseDoubleMatrix expected = new DenseDoubleMatrix(new double[][]{
                {2, 4, 5, 25}, {5, 25, 1, 1}, {7, 49, 25, 625}});
        DenseDoubleMatrix polys = MathUtils.createPolynomials(mat, 2);
        assertEquals(polys.subtract(expected).sum(), 0, 1E-5);
        assertEquals(mat, MathUtils.createPolynomials(mat, 1));
    }

    @Test
    public void testNumericalGradient() {
        // our function is f(x,y) = x^2+y^2
        // the derivative is f'(x,y) = 2x+2y
        final CostFunction inlineFunction = new CostFunction() {
            @Override
            public CostGradientTuple evaluateCost(DoubleVector input) {

                double cost = Math.pow(input.get(0), 2) + Math.pow(input.get(1), 2);
                DenseDoubleVector gradient = new DenseDoubleVector(new double[]{
                        input.get(0) * 2, input.get(1) * 2});

                return new CostGradientTuple(cost, gradient);
            }
        };
        DenseDoubleVector v = new DenseDoubleVector(new double[]{0, 1});
        CostGradientTuple cost = inlineFunction.evaluateCost(v);
        DoubleVector numericalGradient = MathUtils.numericalGradient(v,
                inlineFunction);
        assertSmallDiff(numericalGradient, cost.getGradient());

        v = new DenseDoubleVector(new double[]{-15, 100});
        cost = inlineFunction.evaluateCost(v);

        numericalGradient = MathUtils.numericalGradient(v, inlineFunction);
        assertSmallDiff(numericalGradient, cost.getGradient());
    }

    @Test
    public void testAucComputationAllZeros() throws Exception {
        List<PredictionOutcomePair> outcomePredictedPairs = generateData(1000, 0.9);
        double aucValue = MathUtils.computeAUC(outcomePredictedPairs);
        assertEquals(0.5, aucValue, 1e-6);
    }

    @Test
    public void testAucComputationAllOnes() throws Exception {
        List<PredictionOutcomePair> outcomePredictedPairs = generateData(1000, 0d);
        double aucValue = MathUtils.computeAUC(outcomePredictedPairs);
        assertEquals(1, aucValue, 1e-6);
    }

    public List<PredictionOutcomePair> generateData(int numItems,
                                                    double negativePercentage) {

        Random rand = new Random();
        List<PredictionOutcomePair> outcomePredictedPairs = new ArrayList<>(
                numItems);

        for (int i = 0; i < numItems; i++) {
            outcomePredictedPairs.add(PredictionOutcomePair.from(
                    rand.nextDouble() < negativePercentage ? 1 : 0, 0d));
        }
        return outcomePredictedPairs;
    }

    private void assertSmallDiff(DoubleVector v1, DoubleVector v2) {
        assertEquals(v1.getLength(), v2.getLength());
        for (int i = 0; i < v2.getLength(); i++) {
            double d1 = v2.get(i);
            assertEquals(v1.get(i), d1, 1E-5);
        }
    }

    private void assertSmallDiff(DoubleMatrix v1, DoubleMatrix v2) {
        assertEquals(v1.getColumnCount(), v2.getColumnCount());
        assertEquals(v1.getRowCount(), v2.getRowCount());
        for (int row = 0; row < v1.getRowCount(); row++) {
            for (int col = 0; col < v1.getColumnCount(); col++) {
                double d1 = v2.get(row, col);
                assertEquals(v1.get(row, col), d1, 1E-5);
            }
        }
    }
}
