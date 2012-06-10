package de.jungblut.distance;

import de.jungblut.math.DoubleVector;

public final class JaccardDistance implements DistanceMeasurer {

  @Override
  public double measureDistance(double[] set1, double[] set2) {
    double dot = dot(set1, set2);
    double set1Length = sumOfSquares(set1);
    double set2Length = sumOfSquares(set2);
    return 1.0d - (dot / (set1Length + set2Length - dot));
  }

  private static double dot(double[] set1, double[] set2) {
    double dotProduct = 0.0d;
    for (int i = 0; i < set1.length; i++) {
      dotProduct += set1[i] * set2[i];
    }
    return dotProduct;
  }

  private static double sumOfSquares(double[] set1) {
    double dotProduct = 0.0d;
    for (int i = 0; i < set1.length; i++) {
      dotProduct += set1[i] * set1[i];
    }
    return dotProduct;
  }

  @Override
  public double measureDistance(DoubleVector vec1, DoubleVector vec2) {
    double dot = vec1.dot(vec2);
    double set1Length = vec1.pow(2).sum();
    double set2Length = vec2.pow(2).sum();
    return 1.0d - (dot / (set1Length + set2Length - dot));
  }
}
