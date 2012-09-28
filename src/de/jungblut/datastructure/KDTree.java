package de.jungblut.datastructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.base.Strings;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;

/**
 * Implementation of a kd-tree that handles dense vectors as well as sparse
 * vectors. It offers O(log n) best case lookup time, but can degrade to O(n) if
 * the tree isn't balanced well. It is mostly optimized for special cases like
 * two or three dimensional data, but it does not offer removal of tree nodes
 * (yet).
 * 
 * @author thomas.jungblut
 * 
 */
public final class KDTree implements Iterable<DoubleVector> {

  private KDTreeNode root;

  static final class KDTreeNode {
    final int splitDimension;

    KDTreeNode left;
    KDTreeNode right;

    DoubleVector value;

    public KDTreeNode(int splitDimension, DoubleVector value) {
      this.splitDimension = splitDimension;
      this.value = value;
    }
  }

  // descending sorted by distance, so the head of the prio queue is always
  // largest
  public static final class VectorDistanceTuple implements
      Comparable<VectorDistanceTuple> {

    final DoubleVector value;
    final double dist;

    public VectorDistanceTuple(DoubleVector value, double dist) {
      this.value = value;
      this.dist = dist;
    }

    public double getDistance() {
      return dist;
    }

    public DoubleVector getVector() {
      return value;
    }

    @Override
    public int compareTo(VectorDistanceTuple o) {
      return Double.compare(o.dist, dist);
    }
  }

  /**
   * Adds the given vector to this KD tree.
   */
  public void add(DoubleVector vec) {
    if (root != null) {
      KDTreeNode current = root;
      boolean right = false;
      // traverse the tree to the free spot in dimension
      while (true) {
        right = current.value.get(current.splitDimension) > vec
            .get(current.splitDimension);
        KDTreeNode next = right ? current.right : current.left;
        if (next == null) {
          break;
        } else {
          current = next;
        }
      }
      // do the real insert
      // note that current in this case is the parent
      if (right) {
        current.right = new KDTreeNode(median(vec), vec);
      } else {
        current.left = new KDTreeNode(median(vec), vec);
      }

    } else {
      root = new KDTreeNode(median(vec), vec);
    }
  }

  /**
   * Range queries the kd-tree.
   * 
   * @param lower a lower range bound.
   * @param upper a upper range bound.
   * @return the vectors between the two vectors.
   */
  public List<DoubleVector> rangeQuery(DoubleVector lower, DoubleVector upper) {
    List<DoubleVector> list = Lists.newArrayList();

    // TODO

    return null;
  }

  /**
   * @return the k nearest neighbors to the given vector.
   */
  public List<VectorDistanceTuple> getNearestNeighbours(DoubleVector vec,
      int k, DistanceMeasurer measurer) {
    PriorityQueue<VectorDistanceTuple> queue = new PriorityQueue<>(k);
    KDTreeNode current = root;

    queue.add(new VectorDistanceTuple(current.value, measurer.measureDistance(
        current.value, vec)));
    while (true) {
      if (queue.size() > k)
        queue.remove();
      boolean right = current.value.get(current.splitDimension) > vec
          .get(current.splitDimension);
      KDTreeNode next = right ? current.right : current.left;
      if (next == null) {
        break;
      } else {
        current = next;
        queue.add(new VectorDistanceTuple(current.value, measurer
            .measureDistance(current.value, vec)));
      }
    }

    return new ArrayList<>(queue);
  }

  /**
   * Basic in order traversal.
   */
  @Override
  public Iterator<DoubleVector> iterator() {
    return new AbstractIterator<DoubleVector>() {
      KDTreeNode current;

      @Override
      protected DoubleVector computeNext() {
        if (current == null) {
          current = root;
        } else {
          if (current.left != null) {
            current = current.left;
          }
          if (current.right != null) {
            current = current.right;
          }
          if (current.right == null && current.left == null) {
            return endOfData();
          }
        }

        return current.value;
      }

    };
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    prettyPrintIternal(root, sb, 0);
    return sb.toString();
  }

  private StringBuilder prettyPrintIternal(KDTreeNode node, StringBuilder sb,
      int depth) {
    if (node != null) {
      sb.append("\n" + Strings.repeat("\t", depth));
      sb.append(node.value);
      prettyPrintIternal(node.left, sb, depth + 1);
      prettyPrintIternal(node.right, sb, depth + 1);
    }
    return sb;
  }

  /**
   * @return the index of the median of the vector.
   */
  static int median(DoubleVector v) {
    if (!v.isSparse()) {
      // speedup for two and three dimensional spaces
      if (v.getDimension() == 2) {
        return medianTwoDimensions(v, 0, 1);
      } else if (v.getDimension() == 3) {
        return medianThreeDimensions(v, 0, 1, 2);
      } else {
        // TODO this is pretty much wrong because the array is internally
        // mutated and the returned index is based on that.
        // however the result is astonishing good.
        return ArrayUtils.quickSelect(ArrayUtils.copy(v.toArray()),
            v.getDimension() / 2);
      }
    } else {
      // sparse implementation, basically it finds median on the not zero
      // entries and returns the index.
      final int vectorLength = v.getLength();
      final Iterator<DoubleVectorElement> iterateNonZero = v.iterateNonZero();
      if (vectorLength == 2) {
        return medianTwoDimensions(v, iterateNonZero.next().getIndex(),
            iterateNonZero.next().getIndex());
      } else if (vectorLength == 3) {
        return medianThreeDimensions(v, iterateNonZero.next().getIndex(),
            iterateNonZero.next().getIndex(), iterateNonZero.next().getIndex());
      } else {
        // use the first non-zero index to split on, not a good split, but
        // better than nothing.
        // TODO construct a double heap like BinaryHeap and use it with a median
        // on stream algorithm.
        return iterateNonZero.next().getIndex();

      }
    }
  }

  private static int medianThreeDimensions(DoubleVector v, int i, int j, int k) {
    boolean greater = v.get(i) > v.get(j);
    int largeIndex = greater ? i : j;
    int smallIndex = !greater ? i : j;

    if (v.get(k) > v.get(largeIndex)) {
      return largeIndex;
    } else {
      if (v.get(smallIndex) > v.get(k)) {
        return smallIndex;
      } else {
        return k;
      }
    }
  }

  private static int medianTwoDimensions(DoubleVector v, int i, int j) {
    return v.get(i) > v.get(j) ? i : j;
  }
}
