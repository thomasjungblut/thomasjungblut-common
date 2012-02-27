package de.jungblut.clustering.model;

import org.apache.hama.bsp.BSPMessage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class AverageMessage extends BSPMessage {

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

  @Override
  public final Double getData() {
    return avg;
  }

  @Override
  public final Integer getTag() {
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
