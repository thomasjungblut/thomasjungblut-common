package de.jungblut.math.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hama.bsp.BSPMessage;

public final class ResultMessage extends BSPMessage {

  private int targetRow;
  private int targetColumn;
  private double value;

  public ResultMessage() {

  }

  public ResultMessage(int targetRow, int targetColumn, double value) {
    super();
    this.targetRow = targetRow;
    this.targetColumn = targetColumn;
    this.value = value;
  }

  public int getTargetRow() {
    return targetRow;
  }

  public int getTargetColumn() {
    return targetColumn;
  }

  public double getValue() {
    return value;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    targetRow = in.readInt();
    targetColumn = in.readInt();
    value = in.readDouble();
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(targetRow);
    out.writeInt(targetColumn);
    out.writeDouble(value);
  }

  /*
   * Not used due to object overhead and wrapping
   */

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public Object getTag() {
    return null;
  }

}
