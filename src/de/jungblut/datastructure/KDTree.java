package de.jungblut.datastructure;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.utils.Statistics;

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
public final class KDTree<VALUE> implements Iterable<DoubleVector> {

  private KDTreeNode root;
  private int size;

  final class KDTreeNode {
    final int splitDimension;
    // keyvector by the value in the split dimension
    final DoubleVector keyVector;
    final VALUE value;

    KDTreeNode left;
    KDTreeNode right;

    public KDTreeNode(int splitDimension, DoubleVector keyVector, VALUE val) {
      this.splitDimension = splitDimension;
      this.keyVector = keyVector;
      this.value = val;
    }

    @Override
    public String toString() {
      return "KDTreeNode [splitDimension=" + splitDimension + ", value="
          + keyVector + "]";
    }
  }

  // descending sorted by distance, so the head of the prio queue is always
  // largest
  public static final class VectorDistanceTuple<VALUE> implements
      Comparable<VectorDistanceTuple<VALUE>> {

    final DoubleVector keyVector;
    final VALUE value;
    final double dist;

    public VectorDistanceTuple(DoubleVector keyVector, VALUE value, double dist) {
      this.keyVector = keyVector;
      this.value = value;
      this.dist = dist;
    }

    public double getDistance() {
      return dist;
    }

    public DoubleVector getVector() {
      return keyVector;
    }

    public VALUE getValue() {
      return value;
    }

    @Override
    public int compareTo(VectorDistanceTuple<VALUE> o) {
      return Double.compare(o.dist, dist);
    }

    @Override
    public String toString() {
      return keyVector + " - " + value + " -> " + dist;
    }
  }

  private static final class HyperRectangle {

    protected DoubleVector min;
    protected DoubleVector max;

    public HyperRectangle(DoubleVector min, DoubleVector max) {
      this.min = min;
      this.max = max;
    }

    public DoubleVector closestPoint(DoubleVector t) {
      DoubleVector p = new DenseDoubleVector(t.getDimension());
      for (int i = 0; i < t.getDimension(); ++i) {
        if (t.get(i) <= min.get(i)) {
          p.set(i, min.get(i));
        } else if (t.get(i) >= max.get(i)) {
          p.set(i, max.get(i));
        } else {
          p.set(i, t.get(i));
        }
      }
      return p;
    }

    public static HyperRectangle infiniteHyperRectangle(int dimension) {
      DoubleVector min = new DenseDoubleVector(dimension);
      DoubleVector max = new DenseDoubleVector(dimension);
      for (int i = 0; i < dimension; ++i) {
        min.set(i, Double.NEGATIVE_INFINITY);
        max.set(i, Double.POSITIVE_INFINITY);
      }

      return new HyperRectangle(min, max);
    }

    @Override
    public String toString() {
      return "min: " + min + " ; max: " + max;
    }
  }

  private final class BreadthFirstIterator extends AbstractIterator<KDTreeNode> {

    private final Deque<KDTreeNode> toVisit = new ArrayDeque<>();
    KDTreeNode current;

    public BreadthFirstIterator() {
      toVisit.add(root);
    }

    @Override
    protected KDTreeNode computeNext() {
      current = toVisit.poll();
      if (current != null) {
        if (current.left != null) {
          toVisit.add(current.left);
        }
        if (current.right != null) {
          toVisit.add(current.right);
        }
        return current;
      }
      return endOfData();
    }
  }

  private final class VectorBFSIterator extends AbstractIterator<DoubleVector> {

    private BreadthFirstIterator inOrderIterator;

    public VectorBFSIterator() {
      inOrderIterator = new BreadthFirstIterator();
    }

    @Override
    protected DoubleVector computeNext() {
      KDTreeNode next = inOrderIterator.computeNext();
      return next != null ? next.keyVector : endOfData();
    }

  }

  /**
   * Adds the given vector with a null value to this tree.
   */
  public void add(DoubleVector vec) {
    add(vec, null);
  }

  /**
   * Adds the given vector with a value to this KD tree.
   */
  public void add(DoubleVector vec, VALUE value) {
    if (root != null) {
      KDTreeNode current = root;
      int level = 0;
      boolean right = false;
      // traverse the tree to the free spot in dimension
      while (true) {
        right = current.keyVector.get(current.splitDimension) <= vec
            .get(current.splitDimension);
        KDTreeNode next = right ? current.right : current.left;
        if (next == null) {
          break;
        } else {
          current = next;
        }
        level++;
      }
      // do the real insert
      // note that current in this case is the parent
      if (right) {
        current.right = new KDTreeNode(median(vec, level), vec, value);
      } else {
        current.left = new KDTreeNode(median(vec, level), vec, value);
      }
    } else {
      root = new KDTreeNode(median(vec, 0), vec, value);
    }
    size++;
  }

  /**
   * Balances this kd-tree by sorting along the split dimension and rebuilding
   * the tree.
   */
  public void balanceBySort() {
    @SuppressWarnings("unchecked")
    KDTreeNode[] nodes = (KDTreeNode[]) Array.newInstance(KDTreeNode.class,
        size());
    int index = 0;
    Iterator<KDTreeNode> iterateNodes = iterateNodes();
    while (iterateNodes.hasNext()) {
      nodes[index++] = iterateNodes.next();
    }

    Arrays.sort(nodes, new Comparator<KDTreeNode>() {
      @Override
      public int compare(KDTreeNode o1, KDTreeNode o2) {
        return Doubles.compare(o1.keyVector.get(o1.splitDimension),
            o2.keyVector.get(o2.splitDimension));
      }
    });

    // do an inverse binary search to build up the tree from the root
    root = fix(nodes, 0, nodes.length - 1);
  }

  /**
   * Fixup the tree recursively by divide and conquering the sorted array.
   */
  private KDTreeNode fix(KDTreeNode[] nodes, int start, int end) {
    if (start > end) {
      return null;
    } else {
      int mid = (start + end) >>> 1;
      KDTreeNode midNode = nodes[mid];
      midNode.left = fix(nodes, start, mid - 1);
      midNode.right = fix(nodes, mid + 1, end);
      return midNode;
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
    List<KDTreeNode> rangeInternal = rangeInternal(lower, upper);
    for (KDTreeNode node : rangeInternal) {
      list.add(node.keyVector);
    }
    return list;
  }

  private List<KDTreeNode> rangeInternal(DoubleVector lower, DoubleVector upper) {
    List<KDTreeNode> list = Lists.newArrayList();
    Deque<KDTreeNode> toVisit = new ArrayDeque<>();
    toVisit.add(root);
    while (!toVisit.isEmpty()) {
      KDTreeNode next = toVisit.pop();
      if (strictLower(upper, next.keyVector)
          && strictHigher(lower, next.keyVector)) {
        list.add(next);
      }

      if (next.left != null && checkSubtree(lower, upper, next.left)) {
        toVisit.add(next.left);
      }
      if (next.right != null && checkSubtree(lower, upper, next.right)) {
        toVisit.add(next.right);
      }
    }
    return list;
  }

  /**
   * checks if the given node is inside the range based on the split.
   */
  private boolean checkSubtree(DoubleVector lower, DoubleVector upper,
      KDTreeNode next) {
    if (next != null) {
      boolean greater = lower.get(next.splitDimension) >= next.keyVector
          .get(next.splitDimension);
      boolean lower2 = upper.get(next.splitDimension) >= next.keyVector
          .get(next.splitDimension);
      return greater || lower2;
    }
    return false;
  }

  /**
   * @return the nearest neighbors to the given vector.
   */
  public List<VectorDistanceTuple<VALUE>> getNearestNeighbours(DoubleVector vec) {
    return getNearestNeighbours(vec, Integer.MAX_VALUE);
  }

  /**
   * @return the k nearest neighbors to the given vector.
   */
  public List<VectorDistanceTuple<VALUE>> getNearestNeighbours(
      DoubleVector vec, int k) {
    return getNearestNeighbours(vec, k, Double.MAX_VALUE);
  }

  /**
   * @return the k nearest neighbors to the given vector.
   */
  public List<VectorDistanceTuple<VALUE>> getNearestNeighbours(
      DoubleVector vec, double radius) {
    return getNearestNeighbours(vec, Integer.MAX_VALUE, radius);
  }

  /**
   * @return the k nearest neighbors to the given vector.
   */
  public List<VectorDistanceTuple<VALUE>> getNearestNeighbours(
      DoubleVector vec, int k, double radius) {
    LimitedPriorityQueue<VectorDistanceTuple<VALUE>> queue = new LimitedPriorityQueue<>(
        k);
    HyperRectangle hr = HyperRectangle.infiniteHyperRectangle(vec
        .getDimension());
    getNearestNeighbourInternal(root, vec, hr, radius, k, radius, queue);
    return queue.toList();
  }

  /**
   * Euclidian distance based recursive algorithm for nearest neighbour queries
   * based on Andrew W. Moore.
   */
  private void getNearestNeighbourInternal(KDTreeNode current,
      DoubleVector target, HyperRectangle hyperRectangle,
      double maxDistSquared, int k, final double radius,
      LimitedPriorityQueue<VectorDistanceTuple<VALUE>> queue) {
    if (current == null) {
      return;
    }
    int s = current.splitDimension;
    DoubleVector pivot = current.keyVector;
    double distancePivotToTarget = EuclidianDistance.get().measureDistance(
        pivot, target);

    HyperRectangle leftHyperRectangle = hyperRectangle;
    HyperRectangle rightHyperRectangle = new HyperRectangle(
        hyperRectangle.min.deepCopy(), hyperRectangle.max.deepCopy());
    leftHyperRectangle.max.set(s, pivot.get(s));
    rightHyperRectangle.min.set(s, pivot.get(s));
    boolean left = target.get(s) > pivot.get(s);
    KDTreeNode nearestNode;
    HyperRectangle nearestHyperRectangle;
    KDTreeNode furtherstNode;
    HyperRectangle furtherstHyperRectangle;
    if (left) {
      nearestNode = current.left;
      nearestHyperRectangle = leftHyperRectangle;
      furtherstNode = current.right;
      furtherstHyperRectangle = rightHyperRectangle;
    } else {
      nearestNode = current.right;
      nearestHyperRectangle = rightHyperRectangle;
      furtherstNode = current.left;
      furtherstHyperRectangle = leftHyperRectangle;
    }
    getNearestNeighbourInternal(nearestNode, target, nearestHyperRectangle,
        maxDistSquared, k, radius, queue);

    double distanceSquared = queue.isFull() ? queue.getMaximumPriority()
        : Double.MAX_VALUE;
    maxDistSquared = Math.min(maxDistSquared, distanceSquared);
    DoubleVector closest = furtherstHyperRectangle.closestPoint(target);
    double closestDistance = EuclidianDistance.get().measureDistance(closest,
        target);
    // check subtrees even if they aren't in your maxDist but within our radius
    if (closestDistance < maxDistSquared || closestDistance < radius) {
      if (distancePivotToTarget < distanceSquared) {
        distanceSquared = distancePivotToTarget > 0d ? distancePivotToTarget
            : distanceSquared;
        // check if we are within our defined radius
        if (distancePivotToTarget <= radius) {
          queue.add(new VectorDistanceTuple<>(current.keyVector, current.value,
              distancePivotToTarget), distancePivotToTarget);
        }
        maxDistSquared = queue.isFull() ? queue.getMaximumPriority()
            : Double.MAX_VALUE;
        maxDistSquared = Math.min(maxDistSquared, distanceSquared);
      }
      // now inspect the furthest away node as well
      getNearestNeighbourInternal(furtherstNode, target,
          furtherstHyperRectangle, maxDistSquared, k, radius, queue);
    }
  }

  @Override
  public Iterator<DoubleVector> iterator() {
    return new VectorBFSIterator();
  }

  Iterator<KDTreeNode> iterateNodes() {
    return new BreadthFirstIterator();
  }

  /**
   * @return the size of the kd-tree.
   */
  public int size() {
    return size;
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
      sb.append("\n").append(Strings.repeat("\t", depth));
      sb.append(node.keyVector + " " + node.splitDimension);
      prettyPrintIternal(node.left, sb, depth + 1);
      prettyPrintIternal(node.right, sb, depth + 1);
    }
    return sb;
  }

  /**
   * @return the index of the median of the vector.
   */
  static int median(DoubleVector v, int insertLevel) {
    if (v.getDimension() == 1) {
      return 0;
    }
    if (!v.isSparse()) {
      // speedup for two and three dimensional spaces
      if (v.getDimension() == 2) {
        return medianTwoDimensions(v, 0, 1);
      } else if (v.getDimension() == 3) {
        return medianThreeDimensions(v, 0, 1, 2);
      } else {
        // fall back to modulo on larger vectors
        return (insertLevel + 1) % v.getDimension();
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
        //
        if (v.isSparse()) {
          // use the first non-zero index to split on, not a good split, but
          // better than nothing.
          return iterateNonZero.next().getIndex();
        } else {
          // compute the median from the numbers
          Statistics stats = new Statistics();
          for (double d : v.toArray()) {
            stats.add(d);
          }
          stats.finalizeComputation();
          double median = stats.getMedian();
          // find the index closest to the median
          return v.subtract(median).abs().minIndex();
        }
      }
    }
  }

  static boolean strictHigher(DoubleVector lower, DoubleVector current) {
    Iterator<DoubleVectorElement> iterateNonZero = lower.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      if (current.get(next.getIndex()) < next.getValue())
        return false;
    }
    return true;
  }

  static boolean strictLower(DoubleVector upper, DoubleVector current) {
    Iterator<DoubleVectorElement> iterateNonZero = upper.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      if (current.get(next.getIndex()) > next.getValue())
        return false;
    }
    return true;
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
