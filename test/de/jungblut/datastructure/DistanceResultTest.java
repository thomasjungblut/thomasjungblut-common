package de.jungblut.datastructure;

import junit.framework.TestCase;

import org.junit.Test;

public class DistanceResultTest extends TestCase {

  @Test
  public void test() {
    DistanceResult<String> distanceResult = new DistanceResult<>(0.1, "Test");
    assertEquals("Test", distanceResult.get());
    assertEquals(0.1, distanceResult.getDistance());
    assertEquals("Test | 0.1", distanceResult.toString());
  }

}
