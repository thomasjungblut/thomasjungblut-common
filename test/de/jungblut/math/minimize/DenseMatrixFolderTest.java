package de.jungblut.math.minimize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;

public class DenseMatrixFolderTest {

  @Test
  public void testFoldAndUnfold() {
    DenseDoubleVector referenceFold = new DenseDoubleVector(new double[] { 1.0,
        4.0, 2.0, 5.0, 3.0, 6.0, 7.0, 10.0, 8.0, 11.0, 9.0, 12.0 });
    DenseDoubleMatrix mat1 = new DenseDoubleMatrix(new double[][] {
        { 1, 2, 3 }, { 4, 5, 6 } });
    DenseDoubleMatrix mat2 = new DenseDoubleMatrix(new double[][] {
        { 7, 8, 9 }, { 10, 11, 12 } });

    DenseDoubleVector foldMatrices = DenseMatrixFolder.foldMatrices(mat1, mat2);
    assertEquals(12, foldMatrices.getLength());
    assertEquals(0.0d, referenceFold.subtract(foldMatrices).sum(), 1e-5);

    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        foldMatrices, new int[][] { { 2, 3 }, { 2, 3 } });

    assertEquals(0.0d, unfoldMatrices[0].subtract(mat1).sum(), 1e-5);
    assertEquals(0.0d, unfoldMatrices[1].subtract(mat2).sum(), 1e-5);

  }
}
