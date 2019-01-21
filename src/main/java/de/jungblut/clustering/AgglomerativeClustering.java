package de.jungblut.clustering;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * "Bottom Up" clustering (agglomerative) using average single linkage
 * clustering. This average is paired up with the so called "centroid" method. <br/>
 * Means in normal language: If we merge two points in space with each other, we
 * look for the nearest neighbour (single linkage, defined by the given distance
 * measurer). If we found the nearest neighbour, we merge both together by
 * averaging their coordinates (average single linkage with centroid method). So
 * if point (1,2) is now nearest neighbour to (5,1) we average and receive (3,
 * 1.5) for the next clustering level. If we are now in the next clustering
 * level and say, we found another cluster (10, 14) which is the nearest
 * neighbour to (3, 1.5). We now merge both again to the next level: ( (10+3)/2,
 * (14+1.5)/2) = (6,5, 7,75). This goes until we have just have a single cluster
 * which forms the root of the resulting cluster binary tree. <br/>
 * Few more details about the algorithm: <br/>
 * <li>Nearest neighbour search is greedy, which means that even far away merges
 * are taken into account, if there is no nearest neighbour available anymore.
 * Therefore one may want to add a distance threshold, and just add those
 * unrelated clusters to the next level until they find a good clustering or
 * just ignore them.</li> <br/>
 * <li>Nearest neighbours are found using exhaustive search: for every
 * unclustered node in the level, we look through the whole list of clusters to
 * find the nearest to merge.</li><br/>
 * <li>If nearest neighbour search was unsuccessful (there was no item to
 * cluster anymore), the point/vector is added to the next level directly.</li>
 *
 * @author thomas.jungblut
 */
public final class AgglomerativeClustering {

    private static final Logger LOG = LogManager
            .getLogger(AgglomerativeClustering.class);

    /**
     * Starts the clustering process.
     *
     * @param points           the points to cluster on
     * @param distanceMeasurer the distance measurement to use.
     * @param verbose          if true, costs in each iteration will be printed.
     * @return a list of lists that contains cluster nodes for each level, where
     * the zeroth index is the top of the tree and thus only contains a
     * single clusternode.
     */
    public static List<List<ClusterNode>> cluster(List<DoubleVector> points,
                                                  DistanceMeasurer distanceMeasurer, boolean verbose) {
        return cluster(points, distanceMeasurer, Double.MAX_VALUE, verbose);
    }

    /**
     * Starts the clustering process.
     *
     * @param points            the points to cluster on
     * @param distanceMeasurer  the distance measurement to use.
     * @param distanceThreshold the threshold to not use a nearest neighbour
     *                          anymore and just add it to the next stage. (note that this is
     *                          experimental and may lead to infinite loops as this may not
     *                          converge to a single root item -> therefore this method is
     *                          protected until I find a good solution to detect these stales).
     * @param verbose           if true, costs in each iteration will be printed.
     * @return a list of lists that contains cluster nodes for each level, where
     * the zeroth index is the top of the tree and thus only contains a
     * single clusternode.
     */
    static List<List<ClusterNode>> cluster(List<DoubleVector> points,
                                           DistanceMeasurer distanceMeasurer, double distanceThreshold,
                                           boolean verbose) {
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
                // find the nearest, this is greedy and doesn't always find the best
                // clustering, but is faster then the normal groupwise average linkage.
                int nearest = -1;
                double nearestDistance = Double.MAX_VALUE;
                for (int j = 0; j < currentLevel.size(); j++) {
                    if (!excludeIndex.contains(j)) {
                        ClusterNode cj = currentLevel.get(j);
                        double dist = distanceMeasurer.measureDistance(ci.getMean(),
                                cj.getMean());
                        if (dist < nearestDistance && dist < distanceThreshold) {
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
                LOG.info(iteration + " | Current level contains " + nextLevel.size()
                        + " elements.");
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
     */
    public static class ClusterNode {

        private ClusterNode parent;

        private ClusterNode left;
        private ClusterNode right;

        private final DoubleVector mean;

        private double splitDistance;

        /**
         * Initialize the node with a single vector, mainly used for initializing
         * the bottom.
         */
        ClusterNode(DoubleVector mean) {
            this.mean = mean.deepCopy();
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
         * at merging time.
         */
        public DoubleVector getMean() {
            return this.mean;
        }

        /**
         * @return the distance between left and right cluster. (Based on their
         * means).
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
