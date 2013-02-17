package de.jungblut.utils;

import junit.framework.TestCase;

import org.junit.Test;

public class StatisticsTest extends TestCase {

  @Test
  public void testStatistics() {
    Statistics stats = new Statistics();
    double[] items = new double[] { 2, 4, 4, 4, 5, 5, 7, 9 };
    for (double item : items) {
      stats.add(item);
    }
    stats.finalizeComputation();
    assertEquals(8, stats.getCount());
    assertEquals(40d, stats.getSum());
    assertEquals(2d, stats.getMin());
    assertEquals(9d, stats.getMax());
    assertEquals(5d, stats.getMedian());
    assertEquals(5d, stats.getMean());
    assertEquals(2d, stats.getStandardDeviation());

  }

}
