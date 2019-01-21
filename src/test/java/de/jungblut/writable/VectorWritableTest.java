package de.jungblut.writable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.SingleEntryDoubleVector;
import de.jungblut.math.named.NamedDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

public class VectorWritableTest {

  @Test
  public void testDenseSerDe() throws Exception {
    DenseDoubleVector vec = new DenseDoubleVector(new double[] { 1, 2, 3 });
    DoubleVector check = check(vec);
    assertTrue(check instanceof DenseDoubleVector);
  }

  @Test
  public void testSparseSerDe() throws Exception {
    SparseDoubleVector vec = new SparseDoubleVector(new double[] { 1, 2, 3 });
    DoubleVector check = check(vec);
    assertTrue(check instanceof SparseDoubleVector);
  }

  @Test
  public void testSingleSerDe() throws Exception {
    SingleEntryDoubleVector vec = new SingleEntryDoubleVector(1d);
    DoubleVector check = check(vec);
    assertTrue(check instanceof SingleEntryDoubleVector);
  }

  @Test
  public void testNamedSerDe() throws Exception {
    String name = "myName";
    DoubleVector vec = new NamedDoubleVector(name, new DenseDoubleVector(
        new double[] { 1, 2, 3 }));
    DoubleVector check = check(vec);
    assertTrue(check instanceof NamedDoubleVector);
    assertEquals(name, ((NamedDoubleVector) check).getName());
  }

  public DoubleVector check(DoubleVector vec) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    VectorWritable.writeVector(vec, out);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream in = new DataInputStream(bais);

    DoubleVector readVec = VectorWritable.readVector(in);

    assertEquals(0.0d, vec.subtract(readVec).sum(), 1e-5);
    return readVec;
  }

}
