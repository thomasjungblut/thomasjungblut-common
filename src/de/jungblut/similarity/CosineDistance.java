package de.jungblut.similarity;

public class CosineDistance implements DistanceMeasurer {

  @Override
  public double measureDistance(double[] set1, double[] set2) {
    // from mahout 5.0
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

}
