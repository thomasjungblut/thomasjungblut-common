package de.jungblut.utils;

import gnu.trove.list.array.TDoubleArrayList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;

import com.google.common.base.Preconditions;

/**
 * Small statistics utility to describe data based on its
 * min/max/mean/median/deviation.
 * 
 * @author thomas.jungblut
 * 
 */
public final class Statistics implements Writable {

  private boolean finalized = false;

  private TDoubleArrayList data = new TDoubleArrayList();
  private double min = Double.MAX_VALUE;
  private double max = -Double.MAX_VALUE;
  private double median;
  private double mean;
  private double standardDeviation;
  private double sum;
  private int count;

  // some other metrics
  private double signalToNoise;
  private double coefficientOfVariation;
  private double dispersionIndex;
  private double variance;

  /**
   * Adds a new data item into the current statistics object.
   * 
   * @param item a normal double value.
   */
  public void add(double item) {
    Preconditions.checkState(!finalized);
    sum += item;
    count++;
    min = Math.min(min, item);
    max = Math.max(max, item);
    data.add(item);
  }

  /**
   * Finalize the computation to calculate the mean/median and deviation.
   */
  public void finalizeComputation() {
    if (count > 0) {
      mean = sum / count;
      for (int i = 0; i < data.size(); i++) {
        double f = data.get(i);
        double diff = (f - mean);
        standardDeviation += diff * diff;
      }
      variance = standardDeviation / count;
      standardDeviation = Math.sqrt(variance);
      // signal to noise mean/stddev
      signalToNoise = mean / standardDeviation;
      // coefficient of variation stddev/mean
      coefficientOfVariation = standardDeviation / mean;
      // dispersion index variance/mean
      dispersionIndex = variance / mean;
      double[] array = data.toArray();
      Arrays.sort(array);
      median = array[count / 2];
    }
    data = null;
    finalized = true;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeBoolean(finalized);
    out.writeInt(count);
    out.writeDouble(sum);
    out.writeDouble(min);
    out.writeDouble(max);
    out.writeDouble(median);
    out.writeDouble(mean);
    out.writeDouble(standardDeviation);
    out.writeDouble(variance);
    out.writeDouble(signalToNoise);
    out.writeDouble(coefficientOfVariation);
    out.writeDouble(dispersionIndex);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    finalized = in.readBoolean();
    count = in.readInt();
    sum = in.readDouble();
    min = in.readDouble();
    max = in.readDouble();
    median = in.readDouble();
    mean = in.readDouble();
    standardDeviation = in.readDouble();
    variance = in.readDouble();
    signalToNoise = in.readDouble();
    coefficientOfVariation = in.readDouble();
    dispersionIndex = in.readDouble();
  }

  public double getMin() {
    Preconditions.checkState(finalized);
    return this.min;
  }

  public double getMax() {
    Preconditions.checkState(finalized);
    return this.max;
  }

  public double getMedian() {
    Preconditions.checkState(finalized);
    return this.median;
  }

  public double getMean() {
    Preconditions.checkState(finalized);
    return this.mean;
  }

  public double getStandardDeviation() {
    Preconditions.checkState(finalized);
    return this.standardDeviation;
  }

  public double getVariance() {
    Preconditions.checkState(finalized);
    return this.variance;
  }

  public double getSignalToNoise() {
    Preconditions.checkState(finalized);
    return this.signalToNoise;
  }

  public double getDispersionIndex() {
    Preconditions.checkState(finalized);
    return this.dispersionIndex;
  }

  public double getCoefficientOfVariation() {
    Preconditions.checkState(finalized);
    return this.coefficientOfVariation;
  }

  public double getSum() {
    Preconditions.checkState(finalized);
    return this.sum;
  }

  public int getCount() {
    Preconditions.checkState(finalized);
    return this.count;
  }

  @Override
  public String toString() {
    return "Statistics [Min=" + this.getMin() + ", Max=" + this.getMax()
        + ", Median=" + this.getMedian() + ", Mean=" + this.getMean()
        + ", StandardDeviation=" + this.getStandardDeviation() + ", Variance="
        + this.getVariance() + ", SignalToNoise=" + this.getSignalToNoise()
        + ", DispersionIndex=" + this.getDispersionIndex()
        + ", CoefficientOfVariation=" + this.getCoefficientOfVariation()
        + ", Sum=" + this.getSum() + ", Count=" + this.getCount() + "]";
  }

}
