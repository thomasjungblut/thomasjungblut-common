package de.jungblut.similarity;

public class ManhattanDistance implements DistanceMeasurer {

  @Override
  public double measureDistance(double[] set1, double[] set2) {
    double sum = 0;
    int length = set1.length;
    for (int i = 0; i < length; i++) {
      sum += Math.abs(set1[i] - set2[i]);
    }
    return sum;
  }

}
