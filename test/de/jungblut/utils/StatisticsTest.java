package de.jungblut.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
    Statistics stats = createStatistics();
    assertStatsCorrect(stats);
  }

  @Test
  public void testSerDe() throws IOException {
    Statistics stats = createStatistics();
    assertStatsCorrect(stats);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    stats.write(new DataOutputStream(baos));
    byte[] byteArray = baos.toByteArray();
    ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
    Statistics stats2 = new Statistics();
    stats2.readFields(new DataInputStream(bis));
    assertStatsCorrect(stats2);
  }

  @Test
  public void testEdgeCases() {
    Statistics stats = createStatistics();
    assertStatsCorrect(stats);

    // now add again
    // now we should get an exception
    exception.expect(IllegalStateException.class);
    stats.add(2);
  }

  private Statistics createStatistics() {
    Statistics stats = new Statistics();
    double[] items = new double[] { 2, 4, 4, 4, 5, 5, 7, 9 };
    for (double item : items) {
      stats.add(item);
    }
    stats.finalizeComputation();
    return stats;
  }

  private void assertStatsCorrect(Statistics stats) {
    assertEquals(8, stats.getCount());
    assertEquals(40d, stats.getSum());
    assertEquals(2d, stats.getMin());
    assertEquals(9d, stats.getMax());
    assertEquals(5d, stats.getMedian());
    assertEquals(5d, stats.getMean());
    assertEquals(2d, stats.getStandardDeviation());
    assertEquals(4d, stats.getVariance());
    assertEquals(2.5d, stats.getSignalToNoise());
    assertEquals(0.8d, stats.getDispersionIndex());
    assertEquals(0.4d, stats.getCoefficientOfVariation());
    assertEquals(
        "Statistics [Min=2.0, Max=9.0, Median=5.0, Mean=5.0, StandardDeviation=2.0, Variance=4.0, "
            + "SignalToNoise=2.5, DispersionIndex=0.8, CoefficientOfVariation=0.4, Sum=40.0, Count=8]",
        stats.toString());
  }

}
