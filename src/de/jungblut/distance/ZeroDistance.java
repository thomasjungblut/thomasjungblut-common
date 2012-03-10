package de.jungblut.distance;

import de.jungblut.math.DoubleVector;

public class ZeroDistance implements DistanceMeasurer {

  @Override
  public double measureDistance(double[] set1, double[] set2) {
    return 0.0d;
  }

  @Override
  public double measureDistance(DoubleVector vec1, DoubleVector vec2) {
    return 0.0d;
  }

}
