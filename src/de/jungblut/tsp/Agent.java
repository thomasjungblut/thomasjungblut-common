package de.jungblut.tsp;

import java.util.Random;
import java.util.concurrent.Callable;

import de.jungblut.tsp.AntColonyOptimization.WalkedWay;

public final class Agent implements Callable<WalkedWay> {

  private final AntColonyOptimization instance;
  private final int start;
  private final boolean[] visited;
  private final Random random = new Random(System.nanoTime());
  private final int[] way;

  private double distanceWalked = 0.0d;
  private int toVisit;

  public Agent(AntColonyOptimization instance, int start) {
    super();
    this.instance = instance;
    this.visited = new boolean[instance.matrix.length];
    visited[start] = true;
    toVisit = visited.length - 1;
    this.start = start;
    this.way = new int[visited.length];
  }

  private final int getNextProbableNode(int y) {
    if (toVisit > 0) {
      int danglingUnvisited = -1;
      final double[] weights = new double[visited.length];

      double columnSum = 0.0d;
      for (int i = 0; i < visited.length; i++) {
        columnSum += Math
            .pow(instance.readPheromone(y, i), instance.getAlpha())
            * Math.pow(instance.invertedMatrix[y][i], instance.getBeta());
      }

      double sum = 0.0d;
      for (int x = 0; x < visited.length; x++) {
        if (!visited[x]) {
          weights[x] = calculateProbability(x, y, columnSum);
          sum += weights[x];
          danglingUnvisited = x;
        }
      }

      if (sum == 0.0d)
        return danglingUnvisited;

      // weighted indexing stuff
      double pSum = 0.0d;
      for (int i = 0; i < visited.length; i++) {
        pSum += weights[i] / sum;
        weights[i] = pSum;
      }

      final double r = random.nextDouble();
      for (int i = 0; i < visited.length; i++) {
        if (!visited[i]) {
          if (r <= weights[i]) {
            return i;
          }
        }
      }

    }
    return -1;
  }

  /*
   * (pheromones ^ ALPHA) * ((1/length) ^ BETA) divided by the sum of all rows.
   */
  private final double calculateProbability(int row, int column, double sum) {
    final double p = Math.pow(instance.readPheromone(column, row),
        instance.getAlpha())
        * Math.pow(instance.invertedMatrix[column][row], instance.getBeta());
    return p / sum;
  }

  @Override
  public final WalkedWay call() throws Exception {

    int lastNode = start;
    int next = start;
    int i = 0;
    while ((next = getNextProbableNode(lastNode)) != -1) {
      way[i] = lastNode;
      i++;
      distanceWalked += instance.matrix[lastNode][next];
      final double phero = (instance.getQ() / (distanceWalked));
      instance.adjustPheromone(lastNode, next, phero);
      visited[next] = true;
      lastNode = next;
      toVisit--;
    }
    distanceWalked += instance.matrix[lastNode][start];
    way[i] = lastNode;

    return new WalkedWay(way, distanceWalked);
  }
}
