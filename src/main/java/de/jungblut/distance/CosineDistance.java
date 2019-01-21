package de.jungblut.distance;

import de.jungblut.math.DoubleVector;

// from mahout 5.0
public final class CosineDistance implements DistanceMeasurer {

    @Override
    public double measureDistance(double[] set1, double[] set2) {
        double dotProduct = 0.0;
        double lengthSquaredp1 = 0.0;
        double lengthSquaredp2 = 0.0;
        for (int i = 0; i < set1.length; i++) {
            lengthSquaredp1 += set1[i] * set1[i];
            lengthSquaredp2 += set2[i] * set2[i];
            dotProduct += set1[i] * set2[i];
        }
        double denominator = Math.sqrt(lengthSquaredp1)
                * Math.sqrt(lengthSquaredp2);

        // correct for floating-point rounding errors
        if (denominator < dotProduct) {
            denominator = dotProduct;
        }
        // prevent NaNs
        if (denominator == 0.0d)
            return 1.0;

        return 1.0 - dotProduct / denominator;
    }

    @Override
    public double measureDistance(DoubleVector vec1, DoubleVector vec2) {
        double lengthSquaredv1 = vec1.pow(2).sum();
        double lengthSquaredv2 = vec2.pow(2).sum();

        double dotProduct = vec2.dot(vec1);
        double denominator = Math.sqrt(lengthSquaredv1)
                * Math.sqrt(lengthSquaredv2);

        // correct for floating-point rounding errors
        if (denominator < dotProduct) {
            denominator = dotProduct;
        }

        return 1.0 - dotProduct / denominator;
    }

}
