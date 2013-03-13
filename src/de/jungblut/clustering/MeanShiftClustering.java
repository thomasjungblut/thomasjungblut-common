package de.jungblut.clustering;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Sequential Mean Shift Clustering using a gaussian kernel and euclidian
 * distance measurement.
 * 
 * @author thomasjungblut
 * 
 */
public final class MeanShiftClustering {

  private static final double SQRT_2_PI = FastMath.sqrt(2 * Math.PI);
  private static final EuclidianDistance DISTANCE = new EuclidianDistance();

  /**
   * Clusters a bunch of given points using the Mean Shift algorithm. It first
   * observes possible centers that are within the given windowSize. Once we
   * have initial centers found, we do a meanshift step and afterwards merge
   * centers that are within the mergeWindow of each other. This algorithm is
   * guranteed to converge to a minimum solution.
   * 
   * @param points the points to cluster.
   * @param windowSize the window size to observe points arround the center in.
   *          This is also used to observe initial centers.
   * @param mergeWindow the merge window size, if a pair of centers is within
   *          this mergeWindow the centers are merged together.
   * @param maxIterations the maximum number of iterations to do before
   *          breaking.
   * @param verbose if true, progress will be reported after each iteration.
   * @return the centers of the meanshift algorithm.
   */
  public static List<DoubleVector> cluster(List<DoubleVector> points,
      double windowSize, double mergeWindow, int maxIterations, boolean verbose) {
    // initialize our lookup structure
    KDTree<Integer> kdTree = new KDTree<>();
    // assign an index to each point
    int index = 0;
    for (DoubleVector v : points) {
      kdTree.add(v, index++);
    }
    // start observing the centers
    List<DoubleVector> centers = observeCenters(kdTree, points, windowSize,
        verbose);
    // now iterate over our found centers
    for (int i = 0; i < maxIterations; i++) {
      int converged = meanShift(kdTree, centers, windowSize);
      // merge if centers are within the mergeWindow
      merge(centers, mergeWindow);
      if (verbose) {
        System.out.println("Iteration: " + i
            + " | Remaining centers converging: " + converged + "/"
            + centers.size());
      }
      if (converged == 0) {
        break;
      }
    }

    return centers;
  }

  /**
   * Merges two centers when they are within the given distance of each other.
   */
  private static void merge(List<DoubleVector> centers, double mergeWindow) {
    for (int i = 0; i < centers.size(); i++) {
      DoubleVector referenceVector = centers.get(i);
      // find centers to merge if they are within our merge window
      for (int j = i + 1; j < centers.size(); j++) {
        DoubleVector center = centers.get(j);
        double dist = DISTANCE.measureDistance(referenceVector, center);
        if (dist < mergeWindow) {
          centers.remove(j);
          centers.set(i, referenceVector.add(center).divide(2d));
          // decrement to not omit the following record
          j--;
        }
      }
    }
  }

  /**
   * Core mean shift algorithm.
   * 
   * @param kdTree the kdtree containing the points to cluster.
   * @param centers the already observed centers.
   * @param h the window size "h".
   * @return the number of centers that haven't converged yet.
   */
  private static int meanShift(KDTree<Integer> kdTree,
      List<DoubleVector> centers, double h) {
    int remainingConvergence = 0;
    for (int i = 0; i < centers.size(); i++) {
      DoubleVector v = centers.get(i);
      List<VectorDistanceTuple<Integer>> neighbours = kdTree
          .getNearestNeighbours(v, h);
      double weightSum = 0d;
      DoubleVector numerator = new DenseDoubleVector(v.getLength());
      for (VectorDistanceTuple<Integer> neighbour : neighbours) {
        if (neighbour.getDistance() < h) {
          double normDistance = neighbour.getDistance() / h;
          weightSum -= gaussianGradient(normDistance);
          numerator = numerator.add(neighbour.getVector().multiply(weightSum));
        }
      }
      if (weightSum > 0d) {
        DoubleVector shift = v.divide(numerator);
        DoubleVector newCenter = v.add(shift);
        if (v.subtract(newCenter).abs().sum() > 1e-5) {
          remainingConvergence++;
          // apply the shift
          centers.set(i, newCenter);
        }
      }
    }
    return remainingConvergence;
  }

  /**
   * Small one pass exclusive clustering.
   */
  private static List<DoubleVector> observeCenters(KDTree<Integer> kdTree,
      List<DoubleVector> points, double h, boolean verbose) {
    List<DoubleVector> centers = new ArrayList<>();
    BitSet assignedIndices = new BitSet(kdTree.size());
    // we are doing one pass over the dataset to determine the first centers
    // that are within h range
    for (int i = 0; i < points.size(); i++) {
      if (!assignedIndices.get(i)) {
        DoubleVector v = points.get(i);
        List<VectorDistanceTuple<Integer>> neighbours = kdTree
            .getNearestNeighbours(v, h);
        DoubleVector center = new DenseDoubleVector(v.getLength());
        int added = 0;
        for (VectorDistanceTuple<Integer> neighbour : neighbours) {
          if (!assignedIndices.get(neighbour.getValue())
              && neighbour.getDistance() < h) {
            center = center.add(neighbour.getVector());
            assignedIndices.set(neighbour.getValue());
            added++;
          }
        }
        // so if our sum is positive, we can divide and add the center
        if (added > 1) {
          DoubleVector newCenter = center.divide(added);
          centers.add(newCenter);
          if (verbose && centers.size() % 1000 == 0) {
            System.out.println("#Centers found: " + centers.size());
          }
        }
      }
      assignedIndices.set(i);
    }

    return centers;
  }

  /**
   * @return the gradient of the gaussian with the given standard deviation.
   */
  private static double gaussianGradient(double stddev) {
    return -FastMath.exp(-(stddev * stddev) / 2d) / (SQRT_2_PI * stddev);
  }

}
