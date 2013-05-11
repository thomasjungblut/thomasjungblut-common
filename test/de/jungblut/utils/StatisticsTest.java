package de.jungblut.utils;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StatisticsTest extends TestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

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

  @Test
  public void testEdgeCases() {
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

    // now add again
    // now we should get an exception
    exception.expect(IllegalStateException.class);
    stats.add(2);
  }

}
