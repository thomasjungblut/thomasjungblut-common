package de.jungblut.datastructure;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DistanceResultTest {

  @Test
  public void test() {
    DistanceResult<String> distanceResult = new DistanceResult<>(0.1, "Test");
    assertEquals("Test", distanceResult.get());
    assertEquals(0.1, distanceResult.getDistance(), 1e-5);
    assertEquals("Test | 0.1", distanceResult.toString());
  }

}
