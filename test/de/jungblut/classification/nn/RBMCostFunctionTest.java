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
    DenseDoubleMatrix mat = new DenseDoubleMatrix(new double[][] {
        { 0, 1, 1, 1, 0 }, { 1, 0, 0, 0, 1 }, { 0, 0, 1, 1, 1 },
        { 1, 1, 0, 0, 0 }, { 1, 0, 0, 0, 0 }, { 0, 1, 1, 1, 0 },
        { 0, 0, 0, 0, 0 } });

    RBMCostFunction fnc = new RBMCostFunction(mat, 10);
    WeightMatrix pInput = new WeightMatrix(mat.getColumnCount(), 10);
    DoubleVector theta = Fmincg.minimizeFunction(fnc,
        DenseMatrixFolder.foldMatrices(pInput.getWeights()), 100, true);
    System.out.println(theta);
  }

  public static void main(String[] args) {
    new RBMCostFunctionTest().testCostFunction();
  }
}
