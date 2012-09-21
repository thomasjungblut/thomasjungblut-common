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
    KDTreeNode parent;
    KDTreeNode left;
    KDTreeNode right;

    DoubleVector value;

    public KDTreeNode(int splitDimension, DoubleVector value, KDTreeNode parent) {
      this.splitDimension = splitDimension;
      this.value = value;
      this.parent = parent;
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
        current.right = new KDTreeNode(median(vec), vec, current);
      } else {
        current.left = new KDTreeNode(median(vec), vec, current);
      }

    } else {
      root = new KDTreeNode(median(vec), vec, null);
    }
  }

  /**
   * Removes this vector from the KD tree. It uses equals to determine if it is
   * the same vector like the passed one.
   * 
   * @return true if removed, false if not (found).
   */
  public boolean remove(DoubleVector vec) {
    KDTreeNode current = root;
    // traverse the tree to the free spot in dimension
    while (true) {

      if (vec.equals(current.value)) {
        // TODO remove the root
        // TODO the easiest way is to reinsert the nodes in the subtrees again,
        // however this is way to timeconsuming.
        if (current == root) {
          if (current.right != null) {

          } else if (current.left != null) {

          } else {
            // the root is alone, just clear up this reference
            root = null;
          }
        } else {
          // TODO removal somewhere
          if (current.left != null) {

          } else if (current.right != null) {

          } else {
            // leaf case
            if (current.parent.left == current) {
              current.parent.left = null;
            } else {
              current.parent.right = null;
            }
          }
        }
        return true;
      }
      boolean right = current.value.get(current.splitDimension) > vec
          .get(current.splitDimension);
      KDTreeNode next = right ? current.right : current.left;
      if (next == null) {
        break;
      } else {
        current = next;
      }
    }
    return false;
  }

  /**
   * @return the k nearest neighbours to the given vector.
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
