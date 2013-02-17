package de.jungblut.utils;

import gnu.trove.list.array.TDoubleArrayList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;

/**
 * Small statistics utility to describe data based on its
 * min/max/mean/median/deviation.
 * 
 * @author thomas.jungblut
 * 
 */
public final class Statistics implements Writable {

  private TDoubleArrayList data = new TDoubleArrayList();
  private double min = Double.MAX_VALUE;
  private double max = -Double.MAX_VALUE;
  private double median;
  private double mean;
  private double standardDeviation;
  private double sum;
  private int count;

  /**
   * Adds a new data item into the current statistics object.
   * 
   * @param item a normal double value.
   */
  public void add(double item) {
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
      standardDeviation = Math.sqrt(standardDeviation / count);
    }
    double[] array = data.toArray();
    Arrays.sort(array);
    median = array[count / 2];
    data = null;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeDouble(min);
    out.writeDouble(max);
    out.writeDouble(median);
    out.writeDouble(mean);
    out.writeDouble(standardDeviation);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    min = in.readDouble();
    max = in.readDouble();
    median = in.readDouble();
    mean = in.readDouble();
    standardDeviation = in.readDouble();
  }

  public double getMin() {
    return this.min;
  }

  public double getMax() {
    return this.max;
  }

  public double getMedian() {
    return this.median;
  }

  public double getMean() {
    return this.mean;
  }

  public double getStandardDeviation() {
    return this.standardDeviation;
  }

  public double getSum() {
    return this.sum;
  }

  public int getCount() {
    return this.count;
  }

  @Override
  public String toString() {
    return "Statistics [min=" + this.min + ", max=" + this.max + ", median="
        + this.median + ", mean=" + this.mean + ", deviation="
        + this.standardDeviation + ", sum=" + this.sum + ", count="
        + this.count + "]";
  }

}
