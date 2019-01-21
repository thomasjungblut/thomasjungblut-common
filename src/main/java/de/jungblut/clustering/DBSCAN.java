package de.jungblut.clustering;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;

/**
 * Sequential version of DBSCAN to evaluate if this algorithm is suitable for
 * arbitrary parallelization paradigms that can crunch graphs. <br/>
 * <br/>
 * PLAN: <br/>
 * 
 * 1. compute distance matrix between the points <br/>
 * 2. extract adjacent points via threshold epsilon and minpoints s <br/>
 * 3. run connected components (here BFS)<br/>
 * 4. PROFIT!
 */
public final class DBSCAN {

  private List<DoubleVector> noise;
  private ArrayList<DoubleVector>[] connectedComponents;

  /**
   * Clusters the points.
   * 
   * @param measurer the distance measurer to use.
   * @param minPoints the minimum points in a cluster.
   * @param epsilon the radius of a point to detect other points.
   */
  public ArrayList<DoubleVector>[] cluster(List<DoubleVector> points,
      DistanceMeasurer measurer, int minPoints, double epsilon) {
    // compute the distance matrix
    DoubleMatrix distanceMatrix = generateDistanceMatrix(measurer, points);
    // generate adjacency list
    TIntObjectHashMap<int[]> adjacencyMatrix = generateAdjacencyMatrix(
        distanceMatrix, points, minPoints, epsilon);
    connectedComponents = findConnectedComponents(points, adjacencyMatrix);
    noise = findNoise(points);
    return connectedComponents;
  }

  /**
   * @return the found noise as list of vectors.
   */
  public List<DoubleVector> getNoise() {
    return this.noise;
  }

  /**
   * A distance matrix (NxN) based on n given points and a distance measurer.
   */
  private DoubleMatrix generateDistanceMatrix(DistanceMeasurer measurer,
      List<DoubleVector> pointList) {

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
  private TIntObjectHashMap<int[]> generateAdjacencyMatrix(
      DoubleMatrix distanceMatrix, List<DoubleVector> points, int minPoints,
      double epsilon) {

    TIntObjectHashMap<int[]> adjacencyList = new TIntObjectHashMap<>();
    for (int col = 0; col < distanceMatrix.getColumnCount(); col++) {
      List<Integer> possibleNeighbours = new ArrayList<>();
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
        adjacencyList.put(col, ArrayUtils.toPrimitiveArray(possibleNeighbours));
      }
    }

    return adjacencyList;
  }

  /**
   * Returns a mapping between a cluster ID and its associated points.
   */
  private ArrayList<DoubleVector>[] findConnectedComponents(
      List<DoubleVector> points, TIntObjectHashMap<int[]> adjacencyMatrix) {
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
    @SuppressWarnings("unchecked")
    ArrayList<DoubleVector>[] array = new ArrayList[connectedComponents.size()];

    TIntObjectIterator<int[]> iterator = connectedComponents.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      int[] values = iterator.value();
      ArrayList<DoubleVector> list = new ArrayList<>(values.length);
      for (int val : values) {
        list.add(points.get(val));
      }
      array[iterator.key()] = list;
    }

    return array;
  }

  /**
   * Find the noise in the given connected components, by taking a set
   * difference.
   * 
   * @return a list of points that are classified as noise.
   */
  private List<DoubleVector> findNoise(List<DoubleVector> points) {
    List<DoubleVector> noise = new ArrayList<>();
    HashSet<DoubleVector> set = new HashSet<>();
    for (List<DoubleVector> component : connectedComponents) {
      set.addAll(component);
    }

    for (DoubleVector point : points) {
      if (!set.contains(point)) {
        noise.add(point);
      }
    }
    return noise;
  }

  /**
   * Simple BFS to find out the connected components.
   */
  private TIntHashSet bfs(TIntHashSet set, int start,
      TIntObjectHashMap<int[]> adjacencyMatrix) {
    final Deque<Integer> vertexDeque = new ArrayDeque<>();
    vertexDeque.add(start);
    while (!vertexDeque.isEmpty()) {
      start = vertexDeque.poll();
      int[] is = adjacencyMatrix.get(start);
      // check for null,because not all points may be included
      if (is != null) {
        set.add(start);
        for (int i : is) {
          if (!set.contains(i)) {
            set.add(i);
            vertexDeque.add(i);
          }
        }
      }
    }
    return set;
  }

}
