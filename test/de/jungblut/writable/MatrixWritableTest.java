package de.jungblut.writable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.dense.DenseDoubleMatrix;

public class MatrixWritableTest extends TestCase {

  @Test
  public void testSerDe() throws Exception {
    DenseDoubleMatrix mat = new DenseDoubleMatrix(new double[][] { { 1, 2, 3 },
        { 4, 5, 6 } });

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);

    MatrixWritable.write(mat, out);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream in = new DataInputStream(bais);
    DoubleMatrix readMat = MatrixWritable.read(in);

    assertEquals(0.0d, mat.subtract(readMat).sum());

  }

}
