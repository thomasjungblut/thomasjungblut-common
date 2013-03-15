package de.jungblut.datastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A queue that has limited capacity. Once it hits the maximum defined capacity
 * it will drop off the one with the most cost.
 * 
 * @author thomas.jungblut
 */
public final class LimitedPriorityQueue<T> {

  static class Entry<T> implements Comparable<Entry<T>> {
    final T data;
    final double value;

    public Entry(final T data, final double value) {
      this.data = data;
      this.value = value;
    }

    @Override
    public int compareTo(Entry<T> t) {
      return Double.compare(t.value, this.value);
    }

    @Override
    public String toString() {
      return data.toString();
    }
  };

  private final PriorityQueue<Entry<T>> queue;
  private final int maxCapacity;

  public LimitedPriorityQueue(int capacity) {
    maxCapacity = capacity;
    queue = new PriorityQueue<>();
  }

  public double getMaximumPriority() {
    Entry<T> p = queue.peek();
    return (p == null) ? Double.POSITIVE_INFINITY : p.value;
  }

  public boolean add(T element, double cost) {
    if (isFull()) {
      if (cost > getMaximumPriority()) {
        return false;
      }
      queue.add(new Entry<>(element, cost));
      queue.poll();
    } else {
      queue.add(new Entry<>(element, cost));
    }
    return true;
  }

  public boolean isFull() {
    return queue.size() >= maxCapacity;
  }

  public T peek() {
    Entry<T> p = queue.peek();
    return (p == null) ? null : p.data;
  }

  public boolean isEmpty() {
    return queue.size() == 0;
  }

  public int getSize() {
    return queue.size();
  }

  public PriorityQueue<Entry<T>> getQueue() {
    return this.queue;
  }

  public T poll() {
    Entry<T> p = queue.poll();
    return (p == null) ? null : p.data;
  }

  public List<T> toList() {
    List<T> list = new ArrayList<>(getSize());
    for (Entry<T> entry : queue) {
      list.add(entry.data);
    }
    return list;
  }

  @Override
  public String toString() {
    return queue.toString();
  }
}
