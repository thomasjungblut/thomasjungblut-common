package de.jungblut.ai;

import junit.framework.TestCase;

import org.junit.Test;

public class QLearningTest extends TestCase {

  @Test
  public void testGridWorldExample() {

    double[][] result = new double[][] {
        // first iteration
        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0 },
        // second iteration
        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.25, 10.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0 },
        // last iteration
        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0125, 0.75, 15.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0 } };

    int width = 5;
    int height = 3;
    int startWidth = 0;
    int startHeight = 1;
    int numIterations = 3;
    double learningRate = 0.5;
    double discountFactor = 0.1;
    // no random choice
    QLearning qLearning = new QLearning(width * height, 4, 0f);
    qLearning.addEndState(9, 10);
    qLearning.addEndState(10, -100);
    qLearning.addEndState(11, -100);
    qLearning.addEndState(12, -100);
    qLearning.addEndState(13, -100);
    qLearning.addEndState(14, -100);

    for (int i = 0; i < numIterations; i++) {
      startWidth = 0;
      int currentState = startWidth + (startHeight * width);
      while (!qLearning.isEndState(currentState)) {
        // always go to the right = action on index 1
        qLearning.update(currentState, 1, currentState = (startWidth++)
            + (startHeight * width), learningRate, discountFactor);
      }
      double[][] vals = qLearning.getQValues();
      for (int x = 0; x < width * height; x++) {
        assertEquals(result[i][x], vals[x][1]);
      }
    }

  }

}
