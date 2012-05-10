package de.jungblut.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import de.jungblut.math.DoubleVector;

public final class ClusterCenter implements WritableComparable<ClusterCenter> {

  private DoubleVector center;
  private int kTimesIncremented = 1;
  public int clusterIndex;

  public ClusterCenter() {
    super();
  }

  public ClusterCenter(DoubleVector center) {
    super();
    this.center = center.deepCopy();
  }

  public ClusterCenter(ClusterCenter center) {
    super();
    this.center = center.center.deepCopy();
    this.kTimesIncremented = center.kTimesIncremented;
  }

  public ClusterCenter(VectorWritable center) {
    super();
    this.center = center.getVector().deepCopy();
  }

  public final void plus(VectorWritable c) {
    plus(c.getVector());
  }

  public final void plus(DoubleVector c) {
    center = center.add(c);
    kTimesIncremented++;
  }

  public final void plus(ClusterCenter c) {
    kTimesIncremented += c.kTimesIncremented;
    center = center.add(c.getCenterVector());
  }

  public final void divideByK() {
    center = center.divide(kTimesIncremented);
  }

  public final boolean converged(ClusterCenter c) {
    return calculateError(c.getCenterVector()) > 0;
  }

  public final boolean converged(ClusterCenter c, double error) {
    return calculateError(c.getCenterVector()) > error;
  }

  public final double calculateError(DoubleVector v) {
    return Math.sqrt(center.subtract(v).abs().sum());
  }

  @Override
  public final void write(DataOutput out) throws IOException {
    VectorWritable.writeVector(center, out);
    out.writeInt(kTimesIncremented);
    out.writeInt(clusterIndex);
  }

  @Override
  public final void readFields(DataInput in) throws IOException {
    this.center = VectorWritable.readVector(in);
    kTimesIncremented = in.readInt();
    clusterIndex = in.readInt();
  }

  @Override
  public final int compareTo(ClusterCenter o) {
    return VectorWritable.compareVector(center, o.getCenterVector());
  }

  /**
   * @return the center
   */
  public final DoubleVector getCenterVector() {
    return center;
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((center == null) ? 0 : center.hashCode());
    return result;
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ClusterCenter other = (ClusterCenter) obj;
    if (center == null) {
      if (other.center != null)
        return false;
    } else if (!center.equals(other.center))
      return false;
    return true;
  }

  @Override
  public final String toString() {
    return "ClusterCenter [center=" + center + "]";
  }

}
