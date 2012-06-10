package de.jungblut.distance;

import de.jungblut.math.DoubleVector;

public final class EuclidianDistance implements DistanceMeasurer {

  @Override
  public double measureDistance(double[] set1, double[] set2) {
    double sum = 0;
    int length = set1.length;
    for (int i = 0; i < length; i++) {
      double diff = set2[i] - set1[i];
      // multiplication is faster than Math.pow() for ^2.
      sum += (diff * diff);
    }

    return Math.sqrt(sum);
  }

  @Override
  public double measureDistance(DoubleVector vec1, DoubleVector vec2) {
    return Math.sqrt(vec2.subtract(vec1).pow(2).sum());
  }

}
