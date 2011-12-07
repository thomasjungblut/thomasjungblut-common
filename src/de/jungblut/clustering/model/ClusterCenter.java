package de.jungblut.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.WritableComparable;

public final class ClusterCenter implements WritableComparable<ClusterCenter> {

    // TODO generally we have to add a kind of error of the assigned vectors to
    // the centers

    private Vector center;
    private int kTimesIncremented = 2;

    public ClusterCenter() {
	super();
	this.center = null;
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

    public ClusterCenter average(ClusterCenter c, boolean local) {
	int newk = kTimesIncremented;
	if (!local) {
	    newk += c.kTimesIncremented;
	}
	double[] vector = c.center.getVector();
	double[] thisVector = Arrays.copyOf(center.getVector(),
		center.getVector().length);
	for (int i = 0; i < vector.length; i++) {
	    thisVector[i] = thisVector[i] + (vector[i] / newk)
		    - (thisVector[i] / newk);
	}
	newk++;
	return new ClusterCenter(new Vector(thisVector), newk);
    }

    public boolean converged(ClusterCenter c) {
	return compareTo(c) == 0 ? false : true;
    }

    // compute the squared error for the two centers
    public boolean converged(ClusterCenter c, double error) {
	double err = 0.0d;
	for (int i = 0; i < center.getVector().length; i++) {
	    // use multiplication instead of pow, because this is faster.
	    final double abs = Math.abs(center.getVector()[i]
		    - c.getCenter().getVector()[i]);
	    err += abs * abs;
	}
	err = err / center.getVector().length;
	if (err <= error) {
	    return true;
	}
	return false;
    }

    @Override
    public void write(DataOutput out) throws IOException {
	center.write(out);
	out.writeInt(kTimesIncremented);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
	this.center = new Vector();
	center.readFields(in);
	kTimesIncremented = in.readInt();
    }

    @Override
    public int compareTo(ClusterCenter o) {
	return center.compareTo(o.getCenter());
    }

    /**
     * @return the center
     */
    public Vector getCenter() {
	return center;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((center == null) ? 0 : center.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
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
    public String toString() {
	return "ClusterCenter [center=" + center + "]";
    }

}
