package de.jungblut.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * Sequential canopy clusterer.
 * 
 * @author thomas.jungblut
 */
public final class CanopyClustering {

  /**
   * Creates a list of canopies. Make sure that t1 > t2!
   * 
   * @param points the points to cluster.
   * @param measure the distance measurer to use.
   * @param t1 the outer cluster distance (fuzzy).
   * @param t2 the inner cluster distance (exclusive).
   * @param verbose if true, output about timinings and number of clusters.
   * @return a list of canopy centers.
   */
  public static List<DoubleVector> createCanopies(List<DoubleVector> pPoints,
      DistanceMeasurer measure, double t1, double t2, boolean verbose) {
    Preconditions.checkArgument(t1 > t2, "t1 must be > t2!");

    // use a linked structure, so we can remove the head fast
    LinkedList<DoubleVector> points = new LinkedList<>(pPoints);

    // do the clustering
    List<DoubleVector> canopyList = new ArrayList<>();
    long start = System.currentTimeMillis();
    while (!points.isEmpty()) {
      DoubleVector p1 = points.get(0);
      points.remove(0);
      DoubleVector canopy = p1.deepCopy();
      int assigned = 0;
      // one can speed this up by an inverted index or a kd-tree
      Iterator<DoubleVector> iterator = points.iterator();
      while (iterator.hasNext()) {
        DoubleVector p2 = iterator.next();
        double dist = measure.measureDistance(p1, p2);
        // Put all points that are within distance threshold T1 into the
        // canopy
        if (dist < t1) {
          assigned++;
          canopy.add(p2);
        }
        // Remove from the list all points that are within distance
        // threshold T2 (strongly bound)
        if (dist < t2) {
          iterator.remove();
        }
      }
      // average it
      if (assigned > 0) {
        canopy = canopy.divide(assigned);
      }
      canopyList.add(canopy);
      System.out.println(points.size()
          + " vectors remaining to cluster | Found canopies: "
          + canopyList.size() + " | Took "
          + (System.currentTimeMillis() - start) + "ms!");
      start = System.currentTimeMillis();
    }
    return canopyList;
  }

}
