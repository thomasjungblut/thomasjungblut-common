package de.jungblut.utils;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StatisticsTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testStatistics() {
    Statistics stats = createStatistics(new double[] { 2, 4, 4, 4, 5, 5, 7, 9 });
    assertStatsCorrect(stats);
  }

  @Test
  public void testSerDe() throws IOException {
    Statistics stats = createStatistics(new double[] { 2, 4, 4, 4, 5, 5, 7, 9 });
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
    Statistics stats = createStatistics(new double[] { 2, 4, 4, 4, 5, 5, 7, 9 });
    assertStatsCorrect(stats);

    // now add again
    // now we should get an exception
    exception.expect(IllegalStateException.class);
    stats.add(2);
  }

  @Test
  public void testMedianEven() {
    Statistics stats = new Statistics();
    double[] items = new double[] { 2, 4 };
    for (double item : items) {
      stats.add(item);
    }
    stats.finalizeComputation();

    assertEquals(3, stats.getMedian(), 1e-4);
  }

  @Test
  public void testMedianOdd() {
    Statistics stats = new Statistics();
    double[] items = new double[] { 2, 4, 3 };
    for (double item : items) {
      stats.add(item);
    }
    stats.finalizeComputation();

    assertEquals(3, stats.getMedian(), 1e-4);
  }

  private Statistics createStatistics(double[] elements) {
    Statistics stats = new Statistics();
    for (double item : elements) {
      stats.add(item);
    }
    stats.finalizeComputation();
    return stats;
  }

  private void assertStatsCorrect(Statistics stats) {
    assertEquals(8, stats.getCount());
    assertEquals(40d, stats.getSum(), 1e-4);
    assertEquals(2d, stats.getMin(), 1e-4);
    assertEquals(9d, stats.getMax(), 1e-4);
    assertEquals(4.5d, stats.getMedian(), 1e-4);
    assertEquals(5d, stats.getMean(), 1e-4);
    assertEquals(2d, stats.getStandardDeviation(), 1e-4);
    assertEquals(4d, stats.getVariance(), 1e-4);
    assertEquals(2.5d, stats.getSignalToNoise(), 1e-4);
    assertEquals(0.8d, stats.getDispersionIndex(), 1e-4);
    assertEquals(0.4d, stats.getCoefficientOfVariation(), 1e-4);
    assertEquals(
        "Statistics [Min=2.0, Max=9.0, Median=4.5, Mean=5.0, StandardDeviation=2.0, Variance=4.0, "
            + "SignalToNoise=2.5, DispersionIndex=0.8, CoefficientOfVariation=0.4, Sum=40.0, Count=8]",
        stats.toString());
  }

}
