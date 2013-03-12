package de.jungblut.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.reader.ImageReader;

public final class MeanShiftClustering {

  private static final double SQRT_2_PI = FastMath.sqrt(2 * Math.PI);

  // window size h
  public static List<DoubleVector> cluster(List<DoubleVector> points, double h,
      int maxIterations, boolean verbose) {

    KDTree<Integer> kdTree = new KDTree<>();
    int index = 0;
    for (DoubleVector v : points) {
      kdTree.add(v, index++);
    }

    List<DoubleVector> centers = observeCenters(kdTree, points, h);
    for (int i = 0; i < maxIterations; i++) {
      int converged = meanShift(kdTree, centers, h);
      if (verbose) {
        System.out.println("Iteration: " + i
            + " | Number of centers not converged yet: " + converged + "/"
            + centers.size());
      }
      if (converged == 0) {
        break;
      }
    }

    return centers;
  }

  private static int meanShift(KDTree<Integer> kdTree,
      List<DoubleVector> centers, double h) {
    int remainingConvergence = 0;
    for (DoubleVector v : centers) {
      // TODO radius search
      List<VectorDistanceTuple<Integer>> neighbours = kdTree
          .getNearestNeighbours(v);
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
        // TODO convergence when the shift doesn't change, or when the center
        // doesn't move anymore?
        DoubleVector shift = v.divide(numerator);
        DoubleVector vCopy = v.deepCopy().add(shift);
        if (vCopy.subtract(v).abs().sum() > 1e-5) {
          remainingConvergence++;
          // copy the stuff to the real center
          for (int i = 0; i < v.getLength(); i++) {
            v.set(i, vCopy.get(i));
          }
        }
      }
    }
    return remainingConvergence;
  }

  private static List<DoubleVector> observeCenters(KDTree<Integer> kdTree,
      List<DoubleVector> points, double h) {
    List<DoubleVector> centers = new ArrayList<>();
    BitSet assignedIndices = new BitSet(kdTree.size());
    // we are doing one pass over the dataset to determine the first centers
    // that are within h range
    for (int i = 0; i < points.size(); i++) {
      if (!assignedIndices.get(i)) {
        DoubleVector v = points.get(i);
        // TODO radius search
        List<VectorDistanceTuple<Integer>> neighbours = kdTree
            .getNearestNeighbours(v);
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
        }
      }
      assignedIndices.set(i);
    }

    return centers;
  }

  private static double gaussianGradient(double stddev) {
    return -FastMath.exp(-(stddev * stddev) / 2d) / (SQRT_2_PI * stddev);
  }

  public static void main(String[] args) throws Exception {
    DoubleVector[] luv = ImageReader.readImageAsLUV(ImageIO.read(new File(
        "files/img/lenna.png")));
    List<DoubleVector> cluster = MeanShiftClustering.cluster(
        Arrays.asList(luv), 3d, 100, true);
    System.out.println(cluster.size());
  }

}
