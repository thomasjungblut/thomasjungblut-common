package de.jungblut.clustering;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;

/**
 * Sequential canopy writer. Something borrowed from mahout, enhanced with an
 * inverted index to speed up search for canopies.
 */
public final class CanopyClustering {

  /**
   * Creates a list of canopies. Make sure that t1 > t2!
   */
  public static List<DoubleVector> createCanopies(List<DoubleVector> points,
      DistanceMeasurer measure, double t1, double t2) {

    HashSet<DoubleVector> pointSet = new HashSet<>(points);

    // build inverted index
    TIntObjectHashMap<List<DoubleVector>> index = new TIntObjectHashMap<>();
    for (DoubleVector v : pointSet) {
      Iterator<DoubleVectorElement> iterateNonZero = v.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        List<DoubleVector> list = index.get(next.getIndex());
        if (list == null) {
          list = new ArrayList<>();
          index.put(next.getIndex(), list);
        }
        list.add(v);
      }
    }
    System.out.println("Done with inverted index build!");
    List<DoubleVector> canopyList = new ArrayList<>();
    long start = System.currentTimeMillis();
    while (!pointSet.isEmpty()) {
      DoubleVector p1 = pointSet.iterator().next();
      pointSet.remove(p1);
      removeFromIndex(p1, index);
      List<DoubleVector> allList = new ArrayList<>();
      Iterator<DoubleVectorElement> iterateNonZero = p1.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        List<DoubleVector> list = index.get(next.getIndex());
        if (list != null) {
          for (DoubleVector v : list) {
            if (v != p1 && pointSet.contains(v))
              allList.add(v);
          }
        }
      }
      DoubleVector canopy = p1.deepCopy();
      int assigned = 0;
      for (DoubleVector p2 : allList) {
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
          pointSet.remove(p2);
          removeFromIndex(p2, index);
        }
      }
      System.out.println(pointSet.size() + " to cluster; found canopies: "
          + canopyList.size() + ". Took -> "
          + (System.currentTimeMillis() - start) + " ms!");
      start = System.currentTimeMillis();
      // average it
      if (assigned > 0)
        canopy = canopy.divide(assigned);
      canopyList.add(canopy);
    }
    return canopyList;
  }

  private static void removeFromIndex(DoubleVector p1,
      TIntObjectHashMap<List<DoubleVector>> index) {
    Iterator<DoubleVectorElement> iterateNonZero = p1.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      List<DoubleVector> list = index.get(next.getIndex());
      if (list != null) {
        Iterator<DoubleVector> iterator = list.iterator();
        while (iterator.hasNext()) {
          if (iterator.next() == p1) {
            iterator.remove();
          }
        }
      }
    }
  }

}
