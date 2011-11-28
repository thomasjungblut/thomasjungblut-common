package de.jungblut.clustering.model;

public final class DistanceMeasurer {

    /**
     * Manhattan stuffz.
     * 
     * @param center
     * @param v
     * @return
     */
    public static final double measureDistance(ClusterCenter center, Vector v) {
	double sum = 0;
	int length = v.getVector().length;
	for (int i = 0; i < length; i++) {
	    sum += Math.abs(center.getCenter().getVector()[i]
		    - v.getVector()[i]);
	}

	return sum;
    }

}
