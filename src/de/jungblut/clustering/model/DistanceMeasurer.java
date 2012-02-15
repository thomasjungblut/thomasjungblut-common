package de.jungblut.clustering.model;

public final class DistanceMeasurer {

    public static double measureManhattanDistance(ClusterCenter center,
                                                  Vector v) {
        double sum = 0;
        int length = v.getVector().length;
        for (int i = 0; i < length; i++) {
            sum += Math.abs(center.getCenter().getVector()[i] - v.getVector()[i]);
        }

        return sum;
    }

    public static double measureEuclidianDistance(ClusterCenter center,
                                                  Vector v) {
        double sum = 0;
        int length = v.getVector().length;
        for (int i = 0; i < length; i++) {
            double diff = center.getCenter().getVector()[i] - v.getVector()[i];
            // multiplication should be faster than math pow for ^2.
            sum += (diff * diff);
        }

        return Math.sqrt(sum);
    }
}
