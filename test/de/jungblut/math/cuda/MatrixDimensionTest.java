package de.jungblut.math.cuda;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.dense.DenseDoubleMatrix;

public class MatrixDimensionTest extends TestCase {

  @Test
  public void testDimensionalityNoTranspose() {

    MatrixDimension dimension = new MatrixDimension(
        new DenseDoubleMatrix(2, 3), new DenseDoubleMatrix(15, 2));

    assertEquals(2, dimension.getM());
    assertEquals(2, dimension.getN());
    assertEquals(3, dimension.getK());

    assertEquals(2, dimension.getLdA());
    assertEquals(15, dimension.getLdB());
    assertEquals(2, dimension.getLdC());

    assertEquals(false, dimension.isTransposeA());
    assertEquals(false, dimension.isTransposeB());

  }

  @Test
  public void testDimensionality() {

    MatrixDimension dimension = new MatrixDimension(
        new DenseDoubleMatrix(3, 2), new DenseDoubleMatrix(15, 2), true, false);

    assertEquals(2, dimension.getM());
    assertEquals(2, dimension.getN());
    assertEquals(3, dimension.getK());

    assertEquals(3, dimension.getLdA());
    assertEquals(15, dimension.getLdB());
    assertEquals(2, dimension.getLdC());

    assertEquals(true, dimension.isTransposeA());
    assertEquals(false, dimension.isTransposeB());

    dimension = new MatrixDimension(new DenseDoubleMatrix(3, 2),
        new DenseDoubleMatrix(2, 15), true, true);
    assertEquals(2, dimension.getM());
    assertEquals(2, dimension.getN());
    assertEquals(15, dimension.getK());

    assertEquals(3, dimension.getLdA());
    assertEquals(2, dimension.getLdB());
    assertEquals(2, dimension.getLdC());

    assertEquals(true, dimension.isTransposeA());
    assertEquals(true, dimension.isTransposeB());

    assertEquals(
        "MatrixDimension [m=2, n=2, k=15, ldA=3, ldB=2, ldC=2, transposeA=true, transposeB=true]",
        dimension.toString());

    dimension = new MatrixDimension(new DenseDoubleMatrix(3, 2),
        new DenseDoubleMatrix(2, 15), false, true);
    assertEquals(3, dimension.getM());
    assertEquals(2, dimension.getN());
    assertEquals(2, dimension.getK());

    assertEquals(3, dimension.getLdA());
    assertEquals(2, dimension.getLdB());
    assertEquals(3, dimension.getLdC());

    assertEquals(false, dimension.isTransposeA());
    assertEquals(true, dimension.isTransposeB());

  }

}
