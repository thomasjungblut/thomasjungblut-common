package de.jungblut.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * Sequential version of k-means clustering.
 * 
 * @author thomas.jungblut
 * 
 */
public final class KMeansClustering {

  private final DoubleVector[] centers;
  private final List<DoubleVector> vectors;
  private final int k;

  /**
   * Initializes a new {@link KMeansClustering}.
   * 
   * @param k the number of centers to use.
   * @param vectors the vectors to cluster.
   * @param random true if use random initialization, else it will just pick the
   *          first k vectors.
   */
  public KMeansClustering(int k, DoubleVector[] vectors, boolean random) {
    this(k, Arrays.asList(vectors), random);
  }

  /**
   * Initializes a new {@link KMeansClustering}.
   * 
   * @param k the number of centers to use.
   * @param vectors the vectors to cluster.
   * @param random true if use random initialization, else it will just pick the
   *          first k vectors.
   */
  public KMeansClustering(int k, List<DoubleVector> vectors, boolean random) {
    this.k = k;
    this.vectors = vectors;
    this.centers = new DoubleVector[k];
    if (random) {
      randomInit();
    } else {
      for (int i = 0; i < k; i++) {
        centers[i] = vectors.get(i);
      }
    }
  }

  /**
   * Initializes a new {@link KMeansClustering}.
   * 
   * @param centers initial centers, maybe seeded from {@link CanopyClustering}.
   * @param vectors the vectors to cluster.
   */
  public KMeansClustering(List<DoubleVector> centers, List<DoubleVector> vectors) {
    this.k = centers.size();
    this.vectors = vectors;
    this.centers = new DoubleVector[k];
    for (int i = 0; i < k; i++) {
      this.centers[i] = centers.get(i);
    }
  }

  /**
   * Starts the clustering process.
   * 
   * @param iterations the iterations to cluster.
   * @param distanceMeasurer the distance measurement to use.
   * @param delta is the change in the sum of distances over iterations. If the
   *          difference is lower than delta the iteration will stop.
   * @param if true, costs in each iteration will be printed.
   * @return the assignments to a cluster, each arraylist contains the vectors
   *         at the index of the center that can be retrieved with
   *         {@link #getCenters()}.
   */
  public ArrayList<DoubleVector>[] cluster(int iterations,
      DistanceMeasurer distanceMeasurer, double delta, boolean verbose) {

    @SuppressWarnings("unchecked")
    ArrayList<DoubleVector>[] assignments = new ArrayList[k];
    for (int i = 0; i < assignments.length; i++) {
      assignments[i] = new ArrayList<>();
    }
    double lastCost = Double.MAX_VALUE;
    // now do the main loopings
    for (int iteration = 0; iteration < iterations; iteration++) {
      // clear current assignments
      for (int i = 0; i < assignments.length; i++) {
        assignments[i].clear();
      }
      double cost = 0d;
      // assign the vectors again
      for (DoubleVector v : vectors) {
        int lowestDistantCenter = 0;
        double lowestDistance = Double.MAX_VALUE;
        for (int i = 0; i < centers.length; i++) {
          final double estimatedDistance = distanceMeasurer.measureDistance(
              centers[i], v);
          // check if we have a can assign a new center, because we
          // got a lower distance
          if (estimatedDistance < lowestDistance) {
            lowestDistance = estimatedDistance;
            lowestDistantCenter = i;
          }
        }
        cost += lowestDistance;
        assignments[lowestDistantCenter].add(v);
      }
      // calculate the new centers
      for (int i = 0; i < assignments.length; i++) {
        // only avg if we have something to avg
        if (!assignments[i].isEmpty()) {
          DoubleVector sumVector = assignments[i].get(0);
          for (int n = 1; n < assignments[i].size(); n++) {
            sumVector = sumVector.add(assignments[i].get(n));
          }
          centers[i] = sumVector.divide(assignments[i].size());
        }
      }

      if (verbose) {
        System.out.print("Iteration " + iteration + " | Cost: " + cost + "\r");
      }
      double diff = lastCost - cost;
      if (diff < delta) {
        break;
      }
      lastCost = cost;

    }
    return assignments;
  }

  /**
   * @return the current state of the centers.
   */
  public DoubleVector[] getCenters() {
    return this.centers;
  }

  /**
   * Random inits the centers.
   */
  private void randomInit() {
    Random rnd = new Random();
    int n = vectors.size();
    for (int i = 0; i < k; i++) {
      centers[i] = vectors.get(rnd.nextInt(n));
    }
  }

}
