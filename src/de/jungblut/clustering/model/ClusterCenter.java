package de.jungblut.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public final class ClusterCenter implements WritableComparable<ClusterCenter> {

    private Vector center;

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

    public boolean converged(ClusterCenter c) {
	return compareTo(c) == 0 ? false : true;
    }

    @Override
    public void write(DataOutput out) throws IOException {
	center.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
	this.center = new Vector();
	center.readFields(in);
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
