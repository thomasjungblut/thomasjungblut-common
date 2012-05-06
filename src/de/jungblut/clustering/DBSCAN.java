package de.jungblut.clustering;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.visualize.GnuPlot;

/**
 * Sequential version of DBSCAN to evaluate if this algorithm is suitable for
 * BSP.
 */
public final class DBSCAN {

  /**
   * PLAN: 1. compute distance matrix between the points <br/>
   * 2. extract adjacent points via threshold epsilon and minpoints s <br/>
   * 3. run some kind of connected components <br/>
   * 4. decouple clusters <br/>
   * 5. ??? <br/>
   * 6. PROFIT!
   */

  /**
   * Generates some random double 2D vectors that have a given x and y scaling.
   */
  public List<DenseDoubleVector> generateRandomPoints(int num, double upperX,
      double upperY) {

    List<DenseDoubleVector> list = new ArrayList<>(num);
    Random random = new Random();
    for (int i = 0; i < num; i++) {
      list.add(new DenseDoubleVector(new double[] {
          random.nextDouble() * upperX, random.nextDouble() * upperY }));
    }
    return list;
  }

  /**
   * A distance matrix (NxN) based on n given points and a distance measurer.
   */
  public DenseDoubleMatrix generateDistanceMatrix(DistanceMeasurer measurer,
      List<DenseDoubleVector> pointList) {

    final int n = pointList.size();
    DenseDoubleMatrix matrix = new DenseDoubleMatrix(n, n);

    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        final double distance = measurer.measureDistance(pointList.get(i),
            pointList.get(j));
        matrix.set(i, j, distance);
      }
    }

    return matrix;
  }

  /**
   * Generates an adjacency matrix from the distance matrix, based on min-points
   * and epsilon (maximum distance between two points). <br/>
   * At this point you can see that never assigned points are possible noise.
   */
  public TIntObjectHashMap<int[]> generateAdjacencyMatrix(
      DenseDoubleMatrix distanceMatrix, List<DenseDoubleVector> points,
      int minPoints, double epsilon) {

    TIntObjectHashMap<int[]> adjacencyList = new TIntObjectHashMap<>();
    for (int col = 0; col < distanceMatrix.getColumnCount(); col++) {
      List<Integer> possibleNeighbours = new ArrayList<Integer>();
      for (int row = 0; row < distanceMatrix.getRowCount(); row++) {
        // don't include the same point
        if (row != col) {
          final double distance = distanceMatrix.get(row, col);
          if (distance < epsilon) {
            possibleNeighbours.add(row);
          }
        }
      }
      // if our range scan found at least minPoints, add them to the adjacency
      // list.
      if (possibleNeighbours.size() >= minPoints) {
        adjacencyList.put(col, toArray(possibleNeighbours));
      }
    }

    return adjacencyList;
  }

  private static int[] toArray(List<Integer> list) {
    int[] arr = new int[list.size()];
    for (int i = 0; i < arr.length; i++)
      arr[i] = list.get(i);
    return arr;
  }

  /**
   * Returns a mapping between a cluster ID and its associated points.
   */
  public TIntObjectHashMap<List<DenseDoubleVector>> findConnectedComponents(
      List<DenseDoubleVector> points, TIntObjectHashMap<int[]> adjacencyMatrix) {
    TIntObjectHashMap<int[]> connectedComponents = new TIntObjectHashMap<>();
    TIntHashSet globallyVisitedVertices = new TIntHashSet();
    int clusterId = 0;
    // loop over all known points
    final int size = points.size();
    for (int i = 0; i < size; i++) {
      if (!globallyVisitedVertices.contains(i)) {
        globallyVisitedVertices.add(i);
        TIntHashSet set = new TIntHashSet();
        set = bfs(set, i, adjacencyMatrix);
        if (!set.isEmpty()) {
          connectedComponents.put(clusterId++, set.toArray());
          globallyVisitedVertices.addAll(set);
        }
      }
    }
    // translate the adjacents back to the points
    TIntObjectHashMap<List<DenseDoubleVector>> map = new TIntObjectHashMap<>();

    TIntObjectIterator<int[]> iterator = connectedComponents.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      int[] values = iterator.value();
      List<DenseDoubleVector> list = new ArrayList<>(values.length);
      for (int val : values) {
        list.add(points.get(val));
      }
      System.out.println(iterator.key() + ": " + list);
      map.put(iterator.key(), list);
    }

    return map;
  }

  private List<DenseDoubleVector> findNoise(
      TIntObjectHashMap<List<DenseDoubleVector>> connectedComponents,
      List<DenseDoubleVector> points) {
    List<DenseDoubleVector> noise = new ArrayList<>();
    HashSet<DenseDoubleVector> hashSet = new HashSet<>();
    for (List<DenseDoubleVector> component : connectedComponents.valueCollection()) {
      hashSet.addAll(component);
    }

    for (DenseDoubleVector point : points) {
      if (!hashSet.contains(point)) {
        noise.add(point);
      }
    }
    return noise;
  }

  private TIntHashSet bfs(TIntHashSet set, int start,
      TIntObjectHashMap<int[]> adjacencyMatrix) {
    int[] is = adjacencyMatrix.get(start);
    // check for null,because not all points may be included
    if (is != null) {
      for (int i : is) {
        if (!set.contains(i)) {
          set.add(i);
          bfs(set, i, adjacencyMatrix);
        }
      }
    }
    return set;
  }

  public static void main(String[] args) {
    int numPoints = 100;
    int minPoints = 5;
    double epsilon = 10.0d;
    DistanceMeasurer measurer = new EuclidianDistance();
    DBSCAN clusterer = new DBSCAN();

    List<DenseDoubleVector> points = clusterer.generateRandomPoints(numPoints,
        100, 100);
    DenseDoubleMatrix distanceMatrix = clusterer.generateDistanceMatrix(
        measurer, points);
    // generate adjacency list
    TIntObjectHashMap<int[]> adjacencyMatrix = clusterer
        .generateAdjacencyMatrix(distanceMatrix, points, minPoints, epsilon);
    // find connected components in this graph
    TIntObjectHashMap<List<DenseDoubleVector>> connectedComponents = clusterer
        .findConnectedComponents(points, adjacencyMatrix);
    // reconstruct the noise
    List<DenseDoubleVector> noise = clusterer.findNoise(connectedComponents,
        points);
    System.out.println("Noise: " + noise);
    connectedComponents.put(connectedComponents.size(), noise);
    GnuPlot.drawPoints(connectedComponents);
  }

}
