package de.jungblut.clustering;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * "Bottom Up" clustering (agglomerative) using average linkage clustering.
 * 
 * @author thomas.jungblut
 * 
 */
public final class AgglomerativeClustering {

  /**
   * Starts the clustering process.
   * 
   * @param points the points to cluster on
   * @param distanceMeasurer the distance measurement to use.
   * @param verbose if true, costs in each iteration will be printed.
   * @return a list of lists that contains cluster nodes for each level, where
   *         the zeroth index is the top of the tree and thus only contains a
   *         single clusternode.
   */
  public static List<List<ClusterNode>> cluster(List<DoubleVector> points,
      DistanceMeasurer distanceMeasurer, boolean verbose) {
    List<List<ClusterNode>> levels = new ArrayList<>();
    List<ClusterNode> currentLevel = new ArrayList<>();
    // start by translating the bottom to the internal tree linkage structure
    for (DoubleVector point : points) {
      currentLevel.add(new ClusterNode(point));
    }
    levels.add(currentLevel);
    int iteration = 0;
    while (currentLevel.size() != 1) {
      List<ClusterNode> nextLevel = new ArrayList<>();
      TIntHashSet excludeIndex = new TIntHashSet();
      for (int i = 0; i < currentLevel.size(); i++) {
        // if we had a good match with some other previous node, don't cluster
        // it again
        if (excludeIndex.contains(i)) {
          continue;
        }
        excludeIndex.add(i);
        ClusterNode ci = currentLevel.get(i);
        // find the nearest
        int nearest = -1;
        double nearestDistance = Double.MAX_VALUE;
        for (int j = 0; j < currentLevel.size(); j++) {
          if (!excludeIndex.contains(j)) {
            ClusterNode cj = currentLevel.get(j);
            double dist = distanceMeasurer.measureDistance(ci.getMean(),
                cj.getMean());
            if (dist < nearestDistance) {
              nearest = j;
              nearestDistance = dist;
            }
          }
        }
        if (nearest >= 0) {
          // now merge those two cluster nodes and add them to the next level
          ClusterNode cn = new ClusterNode(ci, currentLevel.get(nearest),
              nearestDistance);
          nextLevel.add(cn);
          excludeIndex.add(nearest);
        } else {
          // if there is nothing to cluster against here, add it again for the
          // next iteration
          nextLevel.add(ci);
        }
      }
      if (verbose) {
        System.out.println(iteration + " | Current level contains "
            + nextLevel.size() + " elements.");
      }
      levels.add(nextLevel);
      currentLevel = nextLevel;
      iteration++;
    }
    Collections.reverse(levels);
    return levels;
  }

  /**
   * Tree structure for containing information about linkages and distances.
   * 
   * @author thomas.jungblut
   * 
   */
  public static class ClusterNode {

    private ClusterNode parent;

    private ClusterNode left;
    private ClusterNode right;

    private DoubleVector mean;

    private double splitDistance;

    /**
     * Initialize the node with a single vector, mainly used for initializing
     * the bottom.
     */
    ClusterNode(DoubleVector mean) {
      this.mean = mean;
      this.splitDistance = 0d;
    }

    /**
     * Initialize the node with a two ClusterNodes
     */
    ClusterNode(ClusterNode node1, ClusterNode node2, double distance) {
      this.mean = (node1.mean.add(node2.mean)).divide(2);
      this.splitDistance = distance;
      this.left = node1;
      this.right = node2;
      left.parent = this;
      right.parent = this;
    }

    /**
     * @return the mean between the two children. Used for distance calculations
     *         at merging time.
     */
    public DoubleVector getMean() {
      return this.mean;
    }

    /**
     * @return the distance between left and right cluster. (Based on their
     *         means).
     */
    public double getSplitDistance() {
      return this.splitDistance;
    }

    /**
     * @return the parent node, null if root.
     */
    public ClusterNode getParent() {
      return this.parent;
    }

    /**
     * @return the left child, null if not present.
     */
    public ClusterNode getLeft() {
      return this.left;
    }

    /**
     * @return the right child, null if not present.
     */
    public ClusterNode getRight() {
      return this.right;
    }

    @Override
    public String toString() {
      return "ClusterNode [mean=" + this.mean + "]";
    }

  }

}
