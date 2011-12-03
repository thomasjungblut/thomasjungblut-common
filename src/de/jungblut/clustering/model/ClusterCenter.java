package de.jungblut.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.WritableComparable;

import de.jungblut.clustering.KMeansBSP;

public final class ClusterCenter implements WritableComparable<ClusterCenter> {

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
	this.center = center;
    }

    public ClusterCenter(Vector center, int k) {
	super();
	this.center = center;
	this.kTimesIncremented = k;
    }

    // summing up, based on averaging in data streams
    // makes a new defensive copy for a clustercenter
    // TODO this can be refactored because we are most of the time averaging
    // against vectors
    public ClusterCenter average(ClusterCenter c, boolean local) {
	int newk = kTimesIncremented;
	if (!local) {
	    newk += c.kTimesIncremented;
	}
	KMeansBSP.LOG.info("Calculating avg of " + this + " to " + c);
	double[] vector = c.center.getVector();
	double[] thisVector = Arrays.copyOf(center.getVector(),
		center.getVector().length);
	for (int i = 0; i < vector.length; i++) {
	    thisVector[i] = thisVector[i] + (vector[i] / newk)
		    - (thisVector[i] / newk);
	}
	KMeansBSP.LOG.info("\treturning with k=" + newk + " and "
		+ Arrays.toString(thisVector));
	newk++;
	return new ClusterCenter(new Vector(thisVector), newk);
    }
    
    public static void main(String[] args) {
	ClusterCenter c = new ClusterCenter(new Vector(7,6));
	c = c.average(new ClusterCenter(new Vector(16,3)), true);
	c = c.average(new ClusterCenter(new Vector(6,5)), true);
	System.out.println(c);
    }

    public boolean converged(ClusterCenter c) {
	return compareTo(c) == 0 ? false : true;
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
