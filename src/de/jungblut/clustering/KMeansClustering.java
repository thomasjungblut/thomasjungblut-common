package de.jungblut.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * Sequential version of k-means clustering.
 * 
 * @author thomas.jungblut
 * 
 */
public final class KMeansClustering {

  private static final Logger LOG = LogManager
      .getLogger(KMeansClustering.class);

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
      Collections.shuffle(vectors);
    }
    for (int i = 0; i < k; i++) {
      centers[i] = vectors.get(i);
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
   * @return the clusters, which contain a center and the assigned vectors.
   */
  public List<Cluster> cluster(int iterations,
      DistanceMeasurer distanceMeasurer, double delta, boolean verbose) {

    final Deque<DoubleVector>[] assignments = setupAssignments();

    double lastCost = Double.MAX_VALUE;
    // now do the main loopings
    for (int iteration = 0; iteration < iterations; iteration++) {
      // clear the assignments
      Arrays.stream(assignments).forEach(Deque::clear);

      // assign the vectors and accumulate the distance
      double cost = IntStream.range(0, vectors.size()).parallel()
          .mapToDouble((x) -> assign(distanceMeasurer, assignments, x)).sum();

      // calculate the new centers
      computeCenters(assignments);

      if (verbose) {
        LOG.info("Iteration " + iteration + " | Cost: " + cost);
      }
      // did we archieve any improvement? if not, break
      double diff = Math.abs(lastCost - cost);
      if (diff < delta) {
        break;
      }
      lastCost = cost;
    }

    // clear the assignments to get a clean state
    Arrays.stream(assignments).forEach(Deque::clear);
    // do another assignment step to get the final clusters
    IntStream.range(0, vectors.size()).parallel()
        .forEach((x) -> assign(distanceMeasurer, assignments, x));

    List<Cluster> lst = new ArrayList<>();
    for (int i = 0; i < centers.length; i++) {
      lst.add(new Cluster(centers[i], new ArrayList<>(assignments[i])));
    }

    return lst;
  }

  public void computeCenters(Deque<DoubleVector>[] assignments) {
    IntStream.range(0, assignments.length).parallel().forEach((i) -> {
      int len = assignments[i].size();
      if (len > 0) {
        DoubleVector sumVector = assignments[i].pop();
        while (!assignments[i].isEmpty()) {
          sumVector = sumVector.add(assignments[i].pop());
        }
        centers[i] = sumVector.divide(len);
      }
    });
  }

  public Deque<DoubleVector>[] setupAssignments() {
    @SuppressWarnings("unchecked")
    Deque<DoubleVector>[] assignments = new Deque[k];
    for (int i = 0; i < assignments.length; i++) {
      assignments[i] = new ConcurrentLinkedDeque<>();
    }
    return assignments;
  }

  public double assign(DistanceMeasurer distanceMeasurer,
      Deque<DoubleVector>[] assignments, int vectorIndex) {
    DoubleVector v = vectors.get(vectorIndex);
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
    assignments[lowestDistantCenter].add(v);
    return lowestDistance;
  }

  /**
   * @return the current state of the centers.
   */
  public DoubleVector[] getCenters() {
    return this.centers;
  }

}
