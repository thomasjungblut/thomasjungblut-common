package de.jungblut.distance;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.math.DoubleVector;

/**
 * Haversine distance implementation that picks up lat/lng in degrees at
 * array/vector index 0 and 1 and returns the distance in meters between those
 * two vectors.
 * 
 * @author thomas.jungblut
 * 
 */
public final class HaversineDistance implements DistanceMeasurer {

  private static final double EARTH_RADIUS_IN_METERS = 6372797.560856;

  @Override
  public final double measureDistance(double[] a, double[] b) {
    // lat must be on index 0 and lng on index 1
    a = Arrays.copyOf(a, 2);
    b = Arrays.copyOf(b, 2);
    // first convert them to radians
    a[0] = a[0] / 180.0 * Math.PI;
    a[1] = a[1] / 180.0 * Math.PI;
    b[0] = b[0] / 180.0 * Math.PI;
    b[1] = b[1] / 180.0 * Math.PI;

    return FastMath.acos(FastMath.sin(a[0]) * FastMath.sin(b[0])
        + FastMath.cos(a[0]) * FastMath.cos(b[0]) * FastMath.cos(a[1] - b[1]))
        * EARTH_RADIUS_IN_METERS;
  }

  @Override
  public final double measureDistance(DoubleVector vec1, DoubleVector vec2) {
    return measureDistance(vec1.toArray(), vec2.toArray());
  }

}
