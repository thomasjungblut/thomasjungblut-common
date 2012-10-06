package de.jungblut.distance;

import de.jungblut.math.DoubleVector;

public final class HaversineDistance implements DistanceMeasurer {

  static final double DEG_TO_RAD = Math.PI / 180d;

  @Override
  public final double measureDistance(double[] a, double[] b) {
    // lat must be on index 0 and long on index 1
    double latitudeArc = (a[0] - b[0]) * DEG_TO_RAD;
    double longitudeArc = (a[1] - a[1]) * DEG_TO_RAD;
    double latitudeH = Math.sin(latitudeArc * 0.5);
    latitudeH *= latitudeH;
    double lontitudeH = Math.sin(longitudeArc * 0.5);
    lontitudeH *= lontitudeH;
    double tmp = Math.cos(a[0] * DEG_TO_RAD) * Math.cos(b[0] * DEG_TO_RAD);
    return 2.0 * Math.asin(Math.sqrt(latitudeH + tmp * lontitudeH));
  }

  @Override
  public final double measureDistance(DoubleVector vec1, DoubleVector vec2) {
    return measureDistance(vec1.toArray(), vec2.toArray());
  }

}
