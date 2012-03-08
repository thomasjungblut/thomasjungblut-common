package de.jungblut.datastructure.trie;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.PriorityQueue;

import de.jungblut.nlp.DocumentSimilarity;
import de.jungblut.similarity.CosineDistance;
import de.jungblut.similarity.DistanceMeasurer;

/**
 * A radix trie implementation that provides cool lookup features.
 * TODO I must test this because of other backed datastructure 
 * @author thomas.jungblut
 * @param <T>
 */
public final class RadixTrie<T> {

  private final RadixTrieNode<T> root = RadixTrieNode.createRootTrieNode();
  private int size;

  private final DocumentSimilarity distance;

  public RadixTrie() {
    super();
    this.distance = DocumentSimilarity.with(new CosineDistance());
  }

  public RadixTrie(DistanceMeasurer distance) {
    super();
    if (distance == null)
      throw new NullPointerException(
          "Distance must not be null! If you don't want the Distance to be computed use \"ZeroDistance\"!");
    this.distance = DocumentSimilarity.with(distance);
  }

  public final MatchResult<T>[] prefixSearch(String key) {
    return prefixSearch(key, 10);
  }

  public final MatchResult<T>[] prefixSearch(String key, int resultSize) {
    key = key.toUpperCase(Locale.ENGLISH);
    PriorityQueue<MatchResult<T>> queue = new PriorityQueue<MatchResult<T>>(
        resultSize);

    RadixTrieNode<T> current = root;
    RadixTrieNode<T> lastVisited = null;
    int keyOffset = 0;
    while (current != null) {
      lastVisited = current;

      if (current.getChildren() != null) {
        for (RadixTrieNode<T> child : current.getChildren()) {
          int index = startsWith(child.getKey(), key, keyOffset);
          if (index > 0) {
            if (index == child.getKey().length()) {
              current = child;
              keyOffset += index;
              break;
            } else {
              current = child;
              keyOffset += index;
              break;
            }
          } else {
            continue;
          }
        }
      }
      if (lastVisited == current) {
        // get every child that is a leaf and calculate a score
        depthFirstSearchForChildren(current, queue, key, resultSize, 0);
        break;
      }
    }

    // copy the results
    if (queue.size() == 0) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      MatchResult<T>[] res = new MatchResult[queue.size()];
      for (int i = queue.size() - 1; i >= 0; i--) {
        res[i] = queue.poll();
      }
      return res;
    }
  }

  private final void depthFirstSearchForChildren(RadixTrieNode<T> node,
      PriorityQueue<MatchResult<T>> queue, String search, int resultSize,
      int depth) {
    if (node.getValue() != null) {
      // if we reached the max result size, give up
      if (resultSize == queue.size() || depth > 2)
        return;

      // TODO this is not correct and must be rewritten
      queue.add(new MatchResult<T>(node.getDoc(), distance
          .measureDocumentSimilarity(new String[] { node.getValue() },
              new String[] { search })));
    } else {
      if (node.getChildren() != null) {
        for (RadixTrieNode<T> child : node.getChildren()) {
          depthFirstSearchForChildren(child, queue, search, resultSize, depth++);
        }
      }
    }
  }

  public final boolean contains(String key) {
    return getDocuments(key) == null ? false : true;
  }

  public final ArrayList<T> get(String key) {
    return getDocuments(key);
  }

  private final ArrayList<T> getDocuments(String key) {
    key = key.toUpperCase(Locale.ENGLISH);
    RadixTrieNode<T> current = root;
    RadixTrieNode<T> lastVisited = null;
    int keyOffset = 0;
    while (current != null) {
      lastVisited = current;
      if (current.getValue() != null && fastEquals(key, current.getValue()))
        return current.getDoc();
      if (current.getChildren() != null) {
        for (RadixTrieNode<T> child : current.getChildren()) {
          int index = startsWith(child.getKey(), key, keyOffset);
          if (index > 0) {
            if (index == child.getKey().length()) {
              if (current.getValue() != null
                  && fastEquals(key, current.getValue()))
                return current.getDoc();
              else {
                current = child;
                keyOffset += index;
                break;
              }
            } else {
              current = child;
              keyOffset += index;
              break;
            }
          } else {
            continue;
          }
        }
      }
      if (lastVisited == current)
        break;
    }
    return null;
  }

  @SafeVarargs
  public final void insert(String key, T... docs) {
    if (key == null || key.isEmpty()) {
      return;
    } else {
      key = key.toUpperCase(Locale.ENGLISH);
      insert(key, key, root, docs);
    }
    size++;
  }

  /**
   * Returns 0 if it does not start with the key, else it returns the index
   * where the strings first start to differ. If they are equal we are returning
   * insertKey.length();
   */
  static final int startsWith(String trieNodeKey, String insertKey,
      int insertKeyOffset) {
    int index = 0;
    for (int i = insertKeyOffset; i < insertKey.length(); i++) {
      if (i > insertKey.length() - 1 || index > trieNodeKey.length() - 1)
        return index;
      if (insertKey.charAt(i) != trieNodeKey.charAt(index)) {
        return index;
      } else {
        index++;
      }
    }

    return index;
  }

  private final void insert(String initialKey, String key,
      RadixTrieNode<T> node, T[] value) {

    int numberOfMatchingCharacters = node.getNumberOfMatchingCharacters(key);

    // we are either at the root node or we need to go down the tree
    if (node.getKey().equals("") == true
        || numberOfMatchingCharacters == 0
        || (numberOfMatchingCharacters < key.length() && numberOfMatchingCharacters >= node
            .getKey().length())) {
      boolean flag = false;
      String newText = key.substring(numberOfMatchingCharacters, key.length());
      if (node.getChildren() != null) {
        for (RadixTrieNode<T> child : node.getChildren()) {
          if (child.getKey().startsWith(newText.charAt(0) + "")) {
            flag = true;
            insert(initialKey, newText, child, value);
            break;
          }
        }
      }

      // just add the node as the child of the current node
      if (flag == false) {
        RadixTrieNode<T> n = RadixTrieNode.createTrieNode(key, key, value);
        n.setKey(newText);
        n.setValue(initialKey);
        n.setDocArray(value);
        node.addChild(n);
      }
    }
    // there is a exact match just make the current node as data node
    else if (numberOfMatchingCharacters == key.length()
        && numberOfMatchingCharacters == node.getKey().length()) {
      node.mergeDocs(value);
      node.setValue(initialKey);
      // decrement size because we are just adding a value to a key
      size--;
    }
    // This node need to be split as the key to be inserted
    // is a prefix of the current node key
    else if (numberOfMatchingCharacters > 0
        && numberOfMatchingCharacters < node.getKey().length()) {
      RadixTrieNode<T> n1 = RadixTrieNode.createTrieNode(key, key, value);
      n1.setKey(node.getKey().substring(numberOfMatchingCharacters,
          node.getKey().length()));
      n1.setValue(node.getValue());
      n1.setDoc(node.getDoc());
      n1.setChildren(node.getChildren());

      node.setKey(key.substring(0, numberOfMatchingCharacters));
      node.setDoc(null);
      node.setValue(null);
      node.purgeListAndAddChild(n1);

      if (numberOfMatchingCharacters < key.length()) {
        RadixTrieNode<T> n2 = RadixTrieNode.createTrieNode(key, key, value);
        n2.setKey(key.substring(numberOfMatchingCharacters, key.length()));
        n2.setValue(initialKey);
        n2.setDocArray(value);
        node.addChild(n2);
      } else {
        node.setDocArray(value);
        node.setValue(initialKey);
      }
    }
    // this key need to be added as the child of the current node
    else {
      RadixTrieNode<T> n = RadixTrieNode.createTrieNode(key, key, value);
      n.setKey(node.getKey().substring(numberOfMatchingCharacters,
          node.getKey().length()));
      n.setChildren(node.getChildren());
      n.setValue(node.getValue());
      node.setKey(key);
      node.setDocArray(value);
      node.addChild(n);
    }
  }

  public final void display() {
    formatNodeTo(new Formatter(System.out), 0, root);
    System.out.println("\n");
  }

  private final boolean fastEquals(String a, String b) {
    if (a == b)
      return true;
    else if (a.length() == b.length()) {
      int index = startsWith(a, b, 0);
      if (index == 0)
        return false;
      else if (a.length() == index)
        return true;
    }
    return false;
  }

  private final void formatNodeTo(Formatter f, int level, RadixTrieNode<T> node) {
    for (int i = 0; i < level; i++) {
      f.format(" ");
    }
    f.format("|");
    for (int i = 0; i < level; i++) {
      f.format("-");
    }

    f.format("%s->%s%n", node.getKey(), node.getValue());

    if (node.getChildren() != null) {
      for (RadixTrieNode<T> child : node.getChildren()) {
        formatNodeTo(f, level + 1, child);
      }
    }
  }

  public final int getSize() {
    return size;
  }

}
