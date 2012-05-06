package de.jungblut.clustering;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
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
  public TIntObjectHashMap<List<DenseDoubleVector>> generateAdjacencyMatrix(
      DenseDoubleMatrix distanceMatrix, List<DenseDoubleVector> points,
      int minPoints, double epsilon) {

    TIntObjectHashMap<List<DenseDoubleVector>> adjacencyList = new TIntObjectHashMap<>();
    for (int col = 0; col < distanceMatrix.getColumnCount(); col++) {
      List<DenseDoubleVector> possibleNeighbours = new ArrayList<DenseDoubleVector>();
      for (int row = 0; row < distanceMatrix.getRowCount(); row++) {
        // don't include the same point
        if (row != col) {
          final double distance = distanceMatrix.get(row, col);
          if (distance < epsilon) {
            possibleNeighbours.add(points.get(row));
          }
        }
      }
      // if our range scan found at least minPoints, add them to the adjacency
      // list.
      if (possibleNeighbours.size() >= minPoints) {
        adjacencyList.put(col, possibleNeighbours);
      }
    }

    return adjacencyList;
  }
  
  public static void main(String[] args) {
    int minPoints = 5;
    double epsilon = 0.1d;
    DistanceMeasurer measurer = new EuclidianDistance();
    DBSCAN clusterer = new DBSCAN();
    
    List<DenseDoubleVector> points = clusterer.generateRandomPoints(1000, 100, 100);
    GnuPlot.drawPoints(points);
    DenseDoubleMatrix distanceMatrix = clusterer.generateDistanceMatrix(measurer, points);
    TIntObjectHashMap<List<DenseDoubleVector>> adjacencyMatrix = clusterer.generateAdjacencyMatrix(distanceMatrix, points, minPoints, epsilon);
    
  }

}
