package de.jungblut.classification.nn;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.dense.DenseDoubleMatrix;

public class WeightMatrixTest extends TestCase {

  @Test
  public void testInit() {
    MultilayerPerceptron.SEED = 0l;
    DenseDoubleMatrix mat = new WeightMatrix(2, 3).getWeights();

    assertEquals(0.532915792500396, mat.get(0, 0));
    assertEquals(0.05996064284821445, mat.get(0, 1));
    assertEquals(0.10130511193101194, mat.get(0, 2));

    assertEquals(0.20115981409625333, mat.get(1, 0));
    assertEquals(0.7585999616576491, mat.get(1, 1));
    assertEquals(0.07173419648944401, mat.get(1, 2));

    assertEquals(-0.3114541695609426, mat.get(2, 0));
    assertEquals(-0.6921393707523671, mat.get(2, 1));
    assertEquals(-0.7019011525722139, mat.get(2, 2));
  }

}
