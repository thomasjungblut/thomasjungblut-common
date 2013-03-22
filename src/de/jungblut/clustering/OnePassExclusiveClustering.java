package de.jungblut.clustering;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;

/**
 * A one pass exclusive clustering algorithm. As the name suggests, the
 * clustering algorithm will iterate once over a constructed kd-tree and find
 * nearest neighbours inside a distance threshold. The found neighbours are
 * going to be put into a bitset and will be omitted from search in the
 * following kd-tree searches. Found clusters are checked against a minimum size
 * and maybe discarded when not reaching the configured threshold. This is
 * considered a very fast algorithm, it can be used instead of
 * {@link CanopyClustering}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class OnePassExclusiveClustering {

  private int minSize;
  private int k;
  private double t1;
  private boolean mergeOverlaps;

  /**
   * Constructs a one pass clustering algorithm. With unlimited maximum number
   * of neighbours retrieved and a minimum cluster size of 2.
   * 
   * @param t1 the maximum distance of neighbourhood.
   */
  public OnePassExclusiveClustering(double t1) {
    this(t1, Integer.MAX_VALUE, 2, false);
  }

  /**
   * Constructs a one pass clustering algorithm.
   * 
   * @param t1 the maximum distance of neighbourhood.
   * @param k the maximum number of neighbours to retrieve inside the t1
   *          threshold.
   * @param minSize the minimum size of a cluster.
   * @param mergeOverlaps if true, overlapping found centers by t1 distance will
   *          be merged.
   */
  public OnePassExclusiveClustering(double t1, int k, int minSize,
      boolean mergeOverlaps) {
    this.t1 = t1;
    this.k = k;
    this.minSize = minSize;
  }

  /**
   * Cluster the given items.
   * 
   * @param values the vectors to cluster.
   * @param verbose if true, outputs progress to STDOUT.
   * @return a list of centers that describe the given vectors.
   */
  public List<DoubleVector> cluster(List<DoubleVector> values, boolean verbose) {
    ArrayList<DoubleVector> centers = new ArrayList<>();
    KDTree<Integer> tree = new KDTree<>();
    int index = 0;
    for (DoubleVector value : values) {
      tree.add(value, index++);
    }
    tree.balanceBySort();

    BitSet set = new BitSet(values.size());
    int items = 0;
    for (int i = 0; i < values.size(); i++) {
      if (!set.get(i)) {
        DoubleVector v = values.get(i);
        DoubleVector center = v.deepCopy();
        List<VectorDistanceTuple<Integer>> nns = tree.getNearestNeighbours(v,
            k, t1);
        int sum = 1;
        for (VectorDistanceTuple<Integer> nn : nns) {
          if (nn.getDistance() < t1 && !set.get(nn.getValue())) {
            sum++;
            set.set(nn.getValue());
            center = center.add(nn.getVector());
          }
        }
        // ignore clusters violating the threshold.
        if (sum >= minSize) {
          DoubleVector newCenter = center.divide(sum);
          if (mergeOverlaps) {
            boolean noOverlap = true;
            // merge overlapping clusters within our t1
            for (int x = 0; x < centers.size(); x++) {
              DoubleVector ref = centers.get(x);
              double dist = EuclidianDistance.get().measureDistance(ref,
                  newCenter);
              if (dist < t1) {
                // average both centers
                centers.set(x, ref.add(newCenter).divide(2d));
                noOverlap = false;
                break;
              }
            }
            if (noOverlap) {
              centers.add(newCenter);
            }
          } else {
            centers.add(newCenter);
          }
        }
        set.set(i);
      }
      items++;
      if (verbose && items % 1000 == 0) {
        String progressString = NumberFormat.getPercentInstance().format(
            items / (double) tree.size());
        System.out.format("Processed %d/%d = %s. Centers found: %d.\n", items,
            tree.size(), progressString, centers.size());
      }
    }
    return centers;
  }
}
