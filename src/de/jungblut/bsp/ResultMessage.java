package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import com.google.common.collect.ComparisonChain;

public final class ResultMessage implements WritableComparable<ResultMessage> {

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

  @Override
  public int compareTo(ResultMessage o) {
    return ComparisonChain.start().compare(targetRow, o.targetRow)
        .compare(targetColumn, o.targetColumn).compare(value, o.value).result();
  }

}
