package de.jungblut.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.WritableComparable;

public final class ClusterCenter implements WritableComparable<ClusterCenter> {

  private Vector center;
  private int kTimesIncremented = 2;

  public ClusterCenter() {
    super();
  }

  public ClusterCenter(ClusterCenter center) {
    super();
    this.center = new Vector(center.center);
  }

  public ClusterCenter(Vector center) {
    super();
    this.center = new Vector(center);
  }

  public ClusterCenter(Vector center, int k) {
    super();
    this.center = center;
    this.kTimesIncremented = k;
  }

  public final void plus(Vector c) {
    double[] vector = c.getVector();
    double[] thisVector = center.getVector();

    for (int i = 0; i < thisVector.length; i++) {
      thisVector[i] = thisVector[i] + vector[i];
    }

    kTimesIncremented++;
  }

  public final void divideByInternalIncrement() {
    double[] thisVector = center.getVector();

    for (int i = 0; i < thisVector.length; i++) {
      thisVector[i] = thisVector[i] / (double) kTimesIncremented;
    }
  }

  public final ClusterCenter average(ClusterCenter c, boolean local) {
    int newk = kTimesIncremented;
    if (!local) {
      newk += c.kTimesIncremented;
    }
    return average(c.getCenter(), newk);
  }

  public final ClusterCenter average(Vector c, int newk) {
    double[] vector = c.getVector();
    double[] thisVector = Arrays.copyOf(center.getVector(),
        center.getVector().length);
    for (int i = 0; i < vector.length; i++) {
      thisVector[i] = thisVector[i] + (vector[i] / newk)
          - (thisVector[i] / newk);
    }
    newk++;
    return new ClusterCenter(new Vector(thisVector), newk);
  }

  public final boolean converged(ClusterCenter c) {
    return calculateError(c.getCenter()) > 0 ? true : false;
  }

  public final boolean converged(ClusterCenter c, double error) {
    return calculateError(c.getCenter()) > error;
  }

  public final double calculateError(Vector v) {
    int length = v.getVector().length;
    double[] vector = center.getVector();
    double[] otherVector = v.getVector();

    double err = 0.0d;
    for (int i = 0; i < length; i++) {
      double abs = Math.abs(vector[i] - otherVector[i]);
      err += (abs * abs);
    }
    return err;
  }

  @Override
  public final void write(DataOutput out) throws IOException {
    center.write(out);
    out.writeInt(kTimesIncremented);
  }

  @Override
  public final void readFields(DataInput in) throws IOException {
    this.center = new Vector();
    center.readFields(in);
    kTimesIncremented = in.readInt();
  }

  @Override
  public final int compareTo(ClusterCenter o) {
    return center.compareTo(o.getCenter());
  }

  /**
   * @return the center
   */
  public final Vector getCenter() {
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
