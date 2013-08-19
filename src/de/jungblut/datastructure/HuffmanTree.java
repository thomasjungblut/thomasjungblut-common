package de.jungblut.datastructure;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.math.sparse.SparseBitVector;

/**
 * Huffman tree coder that takes input from a {@link Multiset} and returns
 * huffman codes {@link #getHuffmanCodes()}. Features generic and object tree
 * representation which uses lots of ram and will be replaced with an array
 * based version soon.
 * 
 * @author thomas.jungblut
 * 
 * @param <VALUE> the value that wants to be encoded.
 */
public final class HuffmanTree<VALUE> {

  class HuffmanTreeNode {

    VALUE value;
    HuffmanTreeNode parent;

    HuffmanTreeNode left;
    HuffmanTreeNode right;

    public boolean isLeaf() {
      return left == null && right == null;
    }

  }

  class HuffmanTreeNodeEntry implements Comparable<HuffmanTreeNodeEntry> {

    final HuffmanTreeNode node;
    final VALUE element;
    final int count;

    HuffmanTreeNodeEntry(HuffmanTreeNode element, int count) {
      this.node = element;
      this.count = count;
      this.element = null;
    }

    HuffmanTreeNodeEntry(VALUE element, int count) {
      this.element = element;
      this.count = count;
      this.node = null;
    }

    public VALUE getElement() {
      return element;
    }

    public HuffmanTreeNode getNode() {
      return this.node;
    }

    public int getCount() {
      return count;
    }

    @Override
    public int compareTo(HuffmanTreeNodeEntry o) {
      return Integer.compare(count, o.count);
    }

    @Override
    public String toString() {
      return "HuffmanTreeNodeEntry [element=" + this.element + ", count="
          + this.count + "]";
    }

  }

  // the root of the tree
  private final HuffmanTreeNode root;
  // cardinality is the tree height, that tells us how many bits are needed to
  // encode every item in this tree.
  private int cardinality;
  // true if already constructed, nothing can be added then
  private boolean constructed;

  public HuffmanTree() {
    this.root = new HuffmanTreeNode();
  }

  /**
   * Bulk inserts the given multiset into this huffman tree.
   * 
   * @param multiSet a multiset that contains value mappings to their frequency.
   */
  public void addAll(Multiset<VALUE> multiSet) {
    Preconditions.checkState(!constructed,
        "You can only bulk insert into a fresh huffman tree!");
    Preconditions.checkArgument(multiSet.size() > 1,
        "The Multiset should at least contain two items!");

    PriorityQueue<HuffmanTreeNodeEntry> queue = new PriorityQueue<>();

    for (Entry<VALUE> entry : multiSet.entrySet()) {
      queue.add(new HuffmanTreeNodeEntry(entry.getElement(), entry.getCount()));
    }

    // always pick the first two least frequent items and merge them together
    while (true) {

      HuffmanTreeNodeEntry second = queue.poll();
      HuffmanTreeNode rightLeaf = second.node;
      if (second.element != null) {
        rightLeaf = new HuffmanTreeNode();
        rightLeaf.value = second.element;
      }

      HuffmanTreeNodeEntry first = queue.poll();
      HuffmanTreeNode leftLeaf = first.node;
      if (first.element != null) {
        leftLeaf = new HuffmanTreeNode();
        leftLeaf.value = first.element;
      }

      HuffmanTreeNode parent = new HuffmanTreeNode();
      leftLeaf.parent = parent;
      parent.left = leftLeaf;
      rightLeaf.parent = parent;
      parent.right = rightLeaf;
      cardinality++;

      HuffmanTreeNodeEntry sumEntry = new HuffmanTreeNodeEntry(parent,
          first.getCount() + second.getCount());
      queue.add(sumEntry);

      // we finish this by setting up the root
      if (queue.size() == 1) {
        leftLeaf.parent = root;
        rightLeaf.parent = root;
        root.left = leftLeaf;
        root.right = rightLeaf;
        break;
      }
    }
    // decrement one for the root level
    cardinality--;

    constructed = true;
  }

  /**
   * Bulk returns all generated Huffman codes as a bit vector representation.
   * 
   * @return the mapping between value and its binary code.
   */
  public Map<VALUE, SparseBitVector> getHuffmanCodes() {
    Map<VALUE, SparseBitVector> map = new HashMap<>();

    traverse(root, new BitSet(getCardinality()), 0, map);

    return map;
  }

  /**
   * Decodes a given vector.
   * 
   * @param vector the bit vector to decode.
   * @return null if the code doesn't exist, or the value if it does.
   */
  public VALUE decode(SparseBitVector vector) {
    HuffmanTreeNode current = root;
    for (int index = 0; index < vector.getDimension(); index++) {
      if (((int) vector.get(index)) != 0) {
        current = current.right;
      } else {
        current = current.left;
      }
      if (current == null) {
        return null;
      } else if (current.isLeaf()) {
        break;
      }
    }

    return current.value;
  }

  /**
   * Recursively traverses this huffman tree and sets bits accordingly.
   * 
   * @param node the current node to visit.
   * @param currentBits the current bits set for this current node.
   * @param currentIndex the current index in the bitset to set.
   * @param map the map in which to insert the result.
   */
  private void traverse(HuffmanTreeNode node, BitSet currentBits,
      int currentIndex, Map<VALUE, SparseBitVector> map) {

    if (node.isLeaf()) {
      // set the bits and insert
      SparseBitVector bitVector = new SparseBitVector(getCardinality());
      for (int i = currentBits.nextSetBit(0); i >= 0; i = currentBits
          .nextSetBit(i + 1)) {
        bitVector.set(i, 1d);
      }
      map.put(node.value, bitVector);
    }

    if (node.left != null) {
      BitSet clone = (BitSet) currentBits.clone();
      traverse(node.left, clone, currentIndex + 1, map);
    }

    if (node.right != null) {
      BitSet clone = (BitSet) currentBits.clone();
      clone.set(currentIndex);
      traverse(node.right, clone, currentIndex + 1, map);
    }

  }

  /**
   * @return the number of bits needed to encode every item in this tree
   *         uniquely.
   */
  public int getCardinality() {
    return this.cardinality;
  }

}
