package de.jungblut.clustering;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * Plain sequential DBSCAN clustering.
 * 
 * @author thomas.jungblut
 * 
 */
public final class DBSCANClustering {

  /**
   * Clusters the given points.
   * 
   * @param points the points to cluster.
   * @param measurer the distance measurer to use.
   * @param minPoints the number of minimum points in a cluster.
   * @param epsilon the radius of a point from which we find neighbours.
   * @return the assignments of the points. Points that are not included are
   *         classified as noise.
   */
  public static List<List<DoubleVector>> cluster(List<DoubleVector> points,
      DistanceMeasurer measurer, int minPoints, double epsilon) {

    List<List<DoubleVector>> clusterList = new ArrayList<>();
    TIntHashSet visited = new TIntHashSet();
    List<DoubleVector> currentCluster = new ArrayList<>();
    final int end = points.size();
    for (int i = 0; i < end; i++) {
      if (!visited.contains(i)) {
        visited.add(i);
        TIntArrayList neighbours = getNearestNeighbours(visited, null, points,
            i, measurer, epsilon);

        if (neighbours.size() > minPoints) {
          currentCluster.add(points.get(i));
          expand(points, measurer, visited, neighbours, currentCluster,
              epsilon, minPoints);
          clusterList.add(currentCluster);
          currentCluster = new ArrayList<>();
        }
      }
    }

    return clusterList;
  }

  private static TIntArrayList getNearestNeighbours(TIntHashSet visited,
      TIntHashSet currentNeighbours, List<DoubleVector> points, int x,
      DistanceMeasurer measurer, double epsilon) {
    TIntArrayList list = new TIntArrayList();
    final DoubleVector ref = points.get(x);
    for (int i = 0; i < points.size(); i++) {
      // filter based on what we've seen at the time
      if (!visited.contains(i)) {
        if (currentNeighbours != null && currentNeighbours.contains(i)) {
          continue;
        }
        double dist = measurer.measureDistance(ref, points.get(i));
        if (dist < epsilon) {
          list.add(i);
        }
      }
    }

    return list;
  }

  private static void expand(List<DoubleVector> points,
      DistanceMeasurer measurer, TIntHashSet visited, TIntArrayList neighbours,
      List<DoubleVector> currentCluster, double epsilon, int minPoints) {
    TIntHashSet currentNeighbours = new TIntHashSet();
    currentNeighbours.addAll(currentNeighbours);
    // note that neighbours is growing while we are iterating over it
    for (int i = 0; i < neighbours.size(); i++) {
      int neighbour = neighbours.get(i);
      if (!visited.contains(neighbour)) {
        visited.add(neighbour);
        TIntArrayList expandedNeighbours = getNearestNeighbours(visited,
            currentNeighbours, points, neighbour, measurer, epsilon);
        // add the expanded list to our neighbours
        currentNeighbours.addAll(expandedNeighbours);
        neighbours.addAll(expandedNeighbours);
        // add the point to our cluster
        currentCluster.add(points.get(neighbour));
      }
    }

  }

  /**
   * Find the noise in the given clustering, by taking a set difference.
   * 
   * @return a list of points that were classified as noise.
   */
  public static List<DoubleVector> findNoise(List<DoubleVector> points,
      List<List<DoubleVector>> clusteringOutput) {
    List<DoubleVector> noise = new ArrayList<>();
    HashSet<DoubleVector> set = new HashSet<>();
    for (List<DoubleVector> component : clusteringOutput) {
      set.addAll(component);
    }

    for (DoubleVector point : points) {
      if (!set.contains(point)) {
        noise.add(point);
      }
    }
    return noise;
  }

}
