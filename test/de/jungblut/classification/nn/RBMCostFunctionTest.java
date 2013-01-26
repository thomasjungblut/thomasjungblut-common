package de.jungblut.classification.nn;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.Fmincg;

public class RBMCostFunctionTest extends TestCase {

  @Test
  public void testCostFunction() {

    // testcase from
    // https://github.com/echen/restricted-boltzmann-machines

    DenseDoubleMatrix mat = new DenseDoubleMatrix(new double[][] {
        { 1, 1, 1, 0, 0, 0 }, { 1, 0, 1, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0 },
        { 0, 0, 1, 1, 1, 0 }, { 0, 0, 1, 1, 0, 0 }, { 0, 0, 1, 1, 1, 0 } });

    RBMCostFunction fnc = new RBMCostFunction(mat, 2);
    WeightMatrix pInput = new WeightMatrix(mat.getColumnCount(), 2);
    DoubleVector theta = Fmincg.minimizeFunction(fnc,
        DenseMatrixFolder.foldMatrices(pInput.getWeights()), 100, true);
    int[][] pms = { { 2, 7 } };
    DenseDoubleMatrix thetaMat = DenseMatrixFolder.unfoldMatrices(theta, pms)[0]
        .transpose();
    for (int i = 0; i < thetaMat.getRowCount(); i++) {
      System.out.println(thetaMat.getRowVector(i));
    }
  }

  public static void main(String[] args) {
    new RBMCostFunctionTest().testCostFunction();
  }
}
