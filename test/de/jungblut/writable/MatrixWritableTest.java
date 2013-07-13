package de.jungblut.writable;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;

public class MatrixWritableTest {

  @Test
  public void testDenseSerDe() throws Exception {
    DenseDoubleMatrix mat = new DenseDoubleMatrix(new double[][] { { 1, 2, 3 },
        { 4, 5, 6 } });

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);

    MatrixWritable.writeDenseMatrix(mat, out);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream in = new DataInputStream(bais);
    DoubleMatrix readMat = MatrixWritable.readDenseMatrix(in);

    assertEquals(0.0d, mat.subtract(readMat).sum(), 1e-4);

  }

  @Test
  public void testSparseSerDe() throws Exception {
    SparseDoubleRowMatrix mat = new SparseDoubleRowMatrix(
        new DenseDoubleMatrix(new double[][] { { 1, 0, 3 }, { 0, 0, 0 },
            { 1, 0, 0 } }));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);

    MatrixWritable.writeSparseMatrix(mat, out);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream in = new DataInputStream(bais);
    DoubleMatrix readMat = MatrixWritable.readSparseMatrix(in);

    assertEquals(0.0d, mat.subtract(readMat).sum(), 1e-4);

  }

}
