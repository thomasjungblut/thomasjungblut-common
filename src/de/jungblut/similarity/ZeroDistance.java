package de.jungblut.similarity;

public class ZeroDistance implements DistanceMeasurer {

  @Override
  public double measureDistance(double[] set1, double[] set2) {
    return 0.0d;
  }

}
