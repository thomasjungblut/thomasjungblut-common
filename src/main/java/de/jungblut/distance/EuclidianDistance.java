package de.jungblut.distance;

import de.jungblut.math.DoubleVector;
import org.apache.commons.math3.util.FastMath;

public final class EuclidianDistance implements DistanceMeasurer {

    private static final EuclidianDistance DISTANCE = new EuclidianDistance();

    @Override
    public double measureDistance(double[] set1, double[] set2) {
        double sum = 0;
        int length = set1.length;
        for (int i = 0; i < length; i++) {
            double diff = set2[i] - set1[i];
            // multiplication is faster than Math.pow() for ^2.
            sum += (diff * diff);
        }

        return FastMath.sqrt(sum);
    }

    @Override
    public double measureDistance(DoubleVector vec1, DoubleVector vec2) {
        if (vec1.isSparse() || vec2.isSparse()) {
            return FastMath.sqrt(vec2.subtract(vec1).pow(2).sum());
        } else {
            // dense vectors usually doesn't do a defensive copy, so it is faster than
            // the implementation above.
            return measureDistance(vec1.toArray(), vec2.toArray());
        }
    }

    /**
     * @return a cached euclidian distance measurer.
     */
    public static EuclidianDistance get() {
        return DISTANCE;
    }

}
