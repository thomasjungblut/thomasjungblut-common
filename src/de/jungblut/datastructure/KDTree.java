package de.jungblut.datastructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.base.Strings;
import com.google.common.collect.AbstractIterator;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * Implementation of a kd-tree.
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
  static final class KDDistanceValue implements Comparable<KDDistanceValue> {

    final DoubleVector value;
    final double dist;

    public KDDistanceValue(DoubleVector value, double dist) {
      this.value = value;
      this.dist = dist;
    }

    @Override
    public int compareTo(KDDistanceValue o) {
      return Double.compare(o.dist, dist);
    }
  }

  /**
   * Adds the given vector to this KD tree.
   * 
   * @param vec
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
   * @return the k nearest neighbours to the given vector.
   */
  public List<DoubleVector> getNearestNeighbours(DoubleVector v, int k,
      DistanceMeasurer measurer) {
    PriorityQueue<KDDistanceValue> queue = new PriorityQueue<>(k);
    searchInternal(v, root, queue, k, measurer);
    List<DoubleVector> list = new ArrayList<>(queue.size());
    for (KDDistanceValue vx : queue) {
      list.add(vx.value);
    }
    return list;
  }

  // TODO make more sophisticated by hyperplane intersection
  private void searchInternal(DoubleVector v, KDTreeNode current,
      PriorityQueue<KDDistanceValue> queue, int k, DistanceMeasurer measurer) {
    if (current != null) {
      queue.add(new KDDistanceValue(current.value, measurer.measureDistance(
          current.value, v)));
      if (queue.size() > k) {
        queue.remove();
      }
      boolean right = current.value.get(current.splitDimension) > v
          .get(current.splitDimension);
      KDTreeNode next = right ? current.right : current.left;
      searchInternal(v, next, queue, k, measurer);
    }
  }

  // basic in order traversal
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
    // speedup for two and three dimensional spaces
    if (v.getDimension() == 2) {
      return v.get(0) > v.get(1) ? 0 : 1;
    } else if (v.getDimension() == 3) {
      boolean greater = v.get(0) > v.get(1);
      int largeIndex = greater ? 0 : 1;
      int smallIndex = !greater ? 0 : 1;

      if (v.get(2) > v.get(largeIndex)) {
        return largeIndex;
      } else {
        if (v.get(smallIndex) > v.get(2)) {
          return smallIndex;
        } else {
          return 2;
        }
      }
    } else {
      // TODO this is pretty much wrong because the array is interally mutated
      // and the returned index is based on that.
      return ArrayUtils.quickSelect(ArrayUtils.copy(v.toArray()),
          v.getDimension() / 2);
    }
  }
}
