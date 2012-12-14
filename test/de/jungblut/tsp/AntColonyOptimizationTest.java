package de.jungblut.tsp;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.tsp.AntColonyOptimization.WalkedWay;

public class AntColonyOptimizationTest extends TestCase {

  @Test
  public void testBerlin52() throws Exception {
    AntColonyOptimization antColonyOptimization = new AntColonyOptimization(
        "files/tsp/berlin52.tsp", 200);
    WalkedWay result = antColonyOptimization.start();
    assertTrue(result.distance < 10000);
  }

}
