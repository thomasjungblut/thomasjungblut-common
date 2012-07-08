package de.jungblut.datastructure;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * A stack that also provides random access lookup of values. It is backed by a
 * Deque(linkedlist) and a HashMap.
 * 
 * @author thomas.jungblut
 * 
 * @param <K>
 * @param <V>
 */
public final class StackMap<K, V> {

  private final HashMap<K, V> map = new HashMap<>();
  private final Deque<K> stack = new LinkedList<>();

  /**
   * Immutable class for a Key/Value tuple.
   */
  public class StackMapEntry<KEY, VALUE> implements Entry<KEY, VALUE> {

    private final KEY key;
    private final VALUE value;

    public StackMapEntry(KEY key, VALUE value) {
      super();
      this.key = key;
      this.value = value;
    }

    @Override
    public KEY getKey() {
      return key;
    }

    @Override
    public VALUE getValue() {
      return value;
    }

    @Override
    public VALUE setValue(VALUE value) {
      throw new UnsupportedOperationException(
          "Seting the value is not allowed in immutable environments!");
    }

  }

  /**
   * HashMap access to get the value for a key.
   */
  public V get(K key) {
    return map.get(key);
  }

  /**
   * Put method which puts the k/v mapping into the map and pushes the key on
   * the stack.
   */
  public V put(K key, V value) {
    if (!map.containsKey(key)) {
      stack.push(key);
      return map.put(key, value);
    } else {
      return null;
    }
  }

  /**
   * Retrieves the first item in the stack, but does not remove it.
   */
  public Entry<K, V> peek() {
    K key = stack.peek();
    V value = map.get(key);
    return new StackMapEntry<>(key, value);
  }

  /**
   * Retrieves the first item in the stack and removes it.
   */
  public Entry<K, V> pop() {
    K key = stack.pop();
    V value = map.remove(key);
    return new StackMapEntry<>(key, value);
  }

}
