package de.jungblut.classification.nn;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.SigmoidActivationFunction;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostGradientTuple;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.GradientDescent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RBMCostFunctionTest {

    static int hiddenUnits = 2;

    static {
        MultilayerPerceptron.SEED = 0L;
    }

    // testcase from
    // https://github.com/echen/restricted-boltzmann-machines
    static DoubleVector[] test = new DoubleVector[]{
            new DenseDoubleVector(new double[]{1, 1, 1, 1, 0, 0, 0}),
            new DenseDoubleVector(new double[]{1, 1, 0, 1, 0, 0, 0}),
            new DenseDoubleVector(new double[]{1, 1, 1, 1, 0, 0, 0}),
            new DenseDoubleVector(new double[]{1, 0, 0, 0, 1, 1, 1}),
            new DenseDoubleVector(new double[]{1, 0, 0, 0, 1, 0, 1}),
            new DenseDoubleVector(new double[]{1, 0, 0, 0, 1, 1, 1})};

    @Test
    public void testGradient() {
        WeightMatrix pInput = new WeightMatrix(test[0].getDimension(),
                hiddenUnits + 1);
        DoubleVector foldMatrices = DenseMatrixFolder.foldMatrices(pInput
                .getWeights());
        RBMCostFunction fnc = new RBMCostFunction(test, 0, 1, hiddenUnits,
                new SigmoidActivationFunction(), TrainingType.CPU, 0d,
                MultilayerPerceptron.SEED, false);

        CostGradientTuple evaluateCost = fnc.evaluateCost(foldMatrices);

        assertEquals(10.62, evaluateCost.getCost(), 1e-2);
        DoubleVector target = new DenseDoubleVector(new double[]{0.0,
                0.027379415757720366, 0.029102968186221934, -0.38090575317687425,
                -0.27799120250510584, -0.05453365605307239, 0.028442797042677864,
                -0.007547440696105356, -0.020996345540311157, 0.23725599589259425,
                0.16279353745280023, 0.021913996227666748, 0.21119663986488538,
                0.14066157414419367, 0.018971946780403166, 0.027585532151946184,
                0.07955487735348872, 0.06242886798699649, 0.018894892958963183,
                0.052146356412991667, 0.04730987967580811, -0.08117434385333744,
                -0.006743308468200778, 0.03846403112496833});

        assertEquals(0d, evaluateCost.getGradient().subtract(target).sum(), 1e-4);

    }

    @Test
    public void testRegularizedGradient() {
        WeightMatrix pInput = new WeightMatrix(test[0].getDimension(),
                hiddenUnits + 1);
        DoubleVector foldMatrices = DenseMatrixFolder.foldMatrices(pInput
                .getWeights());
        RBMCostFunction fnc = new RBMCostFunction(test, 0, 1, hiddenUnits,
                new SigmoidActivationFunction(), TrainingType.CPU, 0.1d,
                MultilayerPerceptron.SEED, false);
        CostGradientTuple evaluateCost = fnc.evaluateCost(foldMatrices);
        assertEquals(10.62, evaluateCost.getCost(), 1e-2);
        DoubleVector target = new DenseDoubleVector(new double[]{0.0,
                0.02692309216175836, 0.028617918716451567, -0.38090575317687425,
                -0.2733580157966874, -0.05362476178552118, 0.028442797042677864,
                -0.0074216500178369334, -0.020646406447972637, 0.23725599589259425,
                0.1600803118285869, 0.021548762957205637, 0.21119663986488538,
                0.13831721457512378, 0.018655747667396447, 0.027585532151946184,
                0.07822896273093058, 0.06138838685387988, 0.018894892958963183,
                0.05127725047277514, 0.04652138168121131, -0.08117434385333744,
                -0.006630919993730764, 0.037822963939552194});

        assertEquals(0d, evaluateCost.getGradient().subtract(target).sum(), 1e-4);
    }

    @Test
    public void testCostFunction() {
        WeightMatrix pInput = new WeightMatrix(test[0].getDimension(),
                hiddenUnits + 1);

        RBMCostFunction fnc = new RBMCostFunction(test, 0, 1, hiddenUnits,
                new SigmoidActivationFunction(), TrainingType.CPU, 0d,
                MultilayerPerceptron.SEED, false);

        DoubleVector theta = GradientDescent.minimizeFunction(fnc,
                DenseMatrixFolder.foldMatrices(pInput.getWeights()), 0.01, 1e-5, 5000,
                false);

        int[][] pms = MultilayerPerceptronCostFunction
                .computeUnfoldParameters(new int[]{test[0].getDimension(),
                        hiddenUnits});
        DoubleMatrix thetaMat = DenseMatrixFolder.unfoldMatrices(theta, pms)[0]
                .transpose();
        double[][] result = new double[][]{
                {0.3768283706784331, -0.429785280688955},
                {-0.019007571880728952, 2.681789402615304},
                {1.853893280384037, 0.8872141826811614},
                {-0.6779189212594092, -2.233814531158892},
                {3.768227750561635, -1.861672501324946},
                {-1.7606548237507884, 2.5544868606627005},
                {-0.9148771784733722, -1.9820382601667268},
                {3.936150254656125, 1.2253565233931112}};

        assertEquals(0, thetaMat.subtract(new DenseDoubleMatrix(result)).sum(),
                1e-4);

    }
}
