package de.jungblut.distance;

import de.jungblut.math.DoubleVector;

/**
 * Similarity measurer wrapper. Basically just constructed by a distance metric
 * and subtracts the distance metric results from 1 to receive the similarity.
 * 
 * @author thomas.jungblut
 * 
 */
public final class SimilarityMeasurer {

  private final DistanceMeasurer measurer;

  public SimilarityMeasurer(DistanceMeasurer measurer) {
    this.measurer = measurer;
  }

  public double measureSimilarity(double[] set1, double[] set2) {
    return 1.0d - measurer.measureDistance(set1, set2);
  }

  public double measureSimilarity(DoubleVector vec1, DoubleVector vec2) {
    return 1.0d - measurer.measureDistance(vec1, vec2);
  }

}
