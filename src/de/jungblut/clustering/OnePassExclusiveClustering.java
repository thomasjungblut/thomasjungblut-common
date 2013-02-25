package de.jungblut.clustering;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.hash.BloomFilter;

import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;
import de.jungblut.utils.VectorFunnel;

/**
 * A one pass exclusive clustering algorithm. As the name suggests, the
 * clustering algorithm will iterate once over a constructed kd-tree and find
 * nearest neighbours inside a distance threshold. The found neighbours are
 * going to be put into a bloom filter and will be omitted from search in the
 * following kd-tree vectors. Found clusters are checked against a minimum size
 * and maybe discarded when exceeding the configured threshold. This is
 * considered a very fast algorithm, it can be used instead of
 * {@link CanopyClustering}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class OnePassExclusiveClustering {

  private DistanceMeasurer measure;
  private int minSize = 2;
  private int k = Integer.MAX_VALUE;
  private double t1 = 1d;

  /**
   * Constructs a one pass clustering algorithm. With unlimited maximum number
   * of neighbours retrieved and a minimum cluster size of 2.
   * 
   * @param measure the distance measurer to use.
   * @param t1 the maximum distance of neighbourhood.
   */
  public OnePassExclusiveClustering(DistanceMeasurer measure, double t1) {
    this.measure = measure;
    this.t1 = t1;
  }

  /**
   * Constructs a one pass clustering algorithm.
   * 
   * @param measure the distance measurer to use.
   * @param t1 the maximum distance of neighbourhood.
   * @param k the maximum number of neighbours to retrieve inside the t1
   *          threshold.
   * @param minSize the minimum size of a cluster.
   */
  public OnePassExclusiveClustering(DistanceMeasurer measure, double t1, int k,
      int minSize) {
    this.measure = measure;
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
    ArrayList<DoubleVector> toReturn = new ArrayList<>();
    KDTree<DoubleVector> tree = new KDTree<>();
    for (DoubleVector value : values) {
      tree.add(value, null);
    }

    BloomFilter<DoubleVector> assigned = BloomFilter.create(new VectorFunnel(),
        tree.size());

    int items = 0;
    Iterator<DoubleVector> iterator = tree.iterator();
    while (iterator.hasNext()) {
      DoubleVector v = iterator.next();
      // zero that vector
      List<VectorDistanceTuple<DoubleVector>> nns = tree.getNearestNeighbours(
          v, k, measure);
      int sum = 0;
      for (VectorDistanceTuple<DoubleVector> nn : nns) {
        if (v != nn.getVector() && nn.getDistance() < t1
            && !assigned.mightContain(nn.getVector())) {
          sum++;
          assigned.put(nn.getVector());
        }
      }
      // ignore clusters violating the threshold.
      if (sum >= minSize) {
        toReturn.add(v);
      }
      assigned.put(v);
      items++;
      if (verbose && items % 1000 == 0) {
        String progressString = NumberFormat.getPercentInstance().format(
            items / (double) tree.size());
        System.out.format("Processed %d/%d = %s. Centers found: %d.\n", items,
            tree.size(), progressString, toReturn.size());
      }
    }
    return toReturn;
  }
}
