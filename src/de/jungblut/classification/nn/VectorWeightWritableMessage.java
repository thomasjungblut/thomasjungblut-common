package de.jungblut.classification.nn;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hama.bsp.BSPMessage;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.writable.MatrixWritable;
import de.jungblut.writable.VectorWritable;

public final class VectorWeightWritableMessage extends BSPMessage {

  private DoubleVector vector;
  private int operations;
  DenseDoubleMatrix[] weights;
  DenseDoubleMatrix[] derivatives;

  public VectorWeightWritableMessage() {
  }

  public VectorWeightWritableMessage(DoubleVector vector, int operations,
      WeightMatrix[] matArray) {
    this.vector = vector;
    this.operations = operations;
    weights = new DenseDoubleMatrix[matArray.length];
    derivatives = new DenseDoubleMatrix[matArray.length];
    for (int i = 0; i < matArray.length; i++) {
      weights[i] = matArray[i].getWeights();
      derivatives[i] = matArray[i].getDerivatives();
    }
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    operations = in.readInt();
    vector = VectorWritable.readVector(in);
    final int length = in.readInt();
    weights = new DenseDoubleMatrix[length];
    derivatives = new DenseDoubleMatrix[length];
    for (int i = 0; i < length; i++) {
      weights[i] = (DenseDoubleMatrix) MatrixWritable.read(in);
      derivatives[i] = (DenseDoubleMatrix) MatrixWritable.read(in);
    }
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(operations);
    VectorWritable.writeVector(vector, out);
    out.writeInt(weights.length);
    for (int i = 0; i < weights.length; i++) {
      MatrixWritable.write(weights[i], out);
      MatrixWritable.write(derivatives[i], out);
    }
  }

  public DenseDoubleMatrix[] getDerivatives() {
    return derivatives;
  }

  public DenseDoubleMatrix[] getWeights() {
    return weights;
  }

  public int getOperations() {
    return operations;
  }

  @Override
  public DoubleVector getData() {
    return vector;
  }

  @Override
  public Object getTag() {
    return null;
  }

}
