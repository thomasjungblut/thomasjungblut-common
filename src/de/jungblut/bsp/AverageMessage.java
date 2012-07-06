package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public final class AverageMessage implements Writable {

  private int k;
  private double avg;

  public AverageMessage() {
    super();
  }

  public AverageMessage(int k, double avg) {
    super();
    this.k = k;
    this.avg = avg;
  }

  @Override
  public final void readFields(DataInput in) throws IOException {
    k = in.readInt();
    avg = in.readDouble();
  }

  @Override
  public final void write(DataOutput out) throws IOException {
    out.writeInt(k);
    out.writeDouble(avg);
  }

  public final double getData() {
    return avg;
  }

  public final int getTag() {
    return k;
  }

  public AverageMessage average(double calculateError) {
    avg = avg + (calculateError / k) - (avg / k);
    k++;
    return this;
  }

  public AverageMessage average(AverageMessage calculateError) {
    this.k = this.k + calculateError.k;
    avg = avg + (calculateError.avg / k) - (avg / k);
    k++;
    return this;
  }

}
