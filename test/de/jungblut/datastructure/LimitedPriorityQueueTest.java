package de.jungblut.datastructure;

import junit.framework.TestCase;

import org.junit.Test;

public class LimitedPriorityQueueTest extends TestCase {

  @Test
  public void testLimitedDrop() throws Exception {

    LimitedPriorityQueue<Integer> queue = new LimitedPriorityQueue<>(3);

    queue.add(1, 5);
    queue.add(2, 15);
    queue.add(3, 25);
    queue.add(4, 5);

    int[] result = new int[] { 2, 4, 1 };
    for (int x : result) {
      assertEquals(x, queue.poll().intValue());
    }

    assertEquals(0, queue.size());

  }
}
