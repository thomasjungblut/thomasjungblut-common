package de.jungblut.ner;

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple3;

public final class IterativeSimilarityAggregationTest extends TestCase {

  String[] terms = new String[] { "A", "B", "C", "D", "E", "F" };
  String[] seedStringSet = new String[] { "A", "B" };
  int[] seedSet = new int[] { 0, 1 };
  DenseDoubleMatrix similarityMatrix = new DenseDoubleMatrix(new double[][] {
      { 1, 0.5, 0.8, 0.7, 0.6, 0.7 }, { 0.5, 1, 0.6, 0.7, 0.7, 0.5 },
      { 0.8, 0.6, 1, 0, 0.9, 0.3 }, { 0.7, 0.7, 0.9, 0.8, 1, 0.4 },
      { 0.3, 0.5, 0.3, 0.5, 0.4, 1 } });
  DenseDoubleVector relevanceScoreResult = new DenseDoubleVector(new double[] {
      0.75, 0.75, 0.7, 0.7, 0.65, 0.6 });

  DenseDoubleVector rankingResult = new DenseDoubleVector(new double[] { 0.75,
      0.725, 0.7625, 0.625, 0.725, 0.5375 });

  double threshold = 0.69;
  double alpha = 0.5;

  @Test
  public void testRelevanceComputation() {
    DenseDoubleVector relevanceScore = IterativeSimilarityAggregation
        .computeRelevanceScore(seedSet, similarityMatrix);
    double sum = relevanceScore.subtract(relevanceScoreResult).abs().sum();
    assertTrue(sum >= 0.0d);
    assertTrue(sum < 0.001d);
  }

  @Test
  public void testThresholding() {
    int[] filterRelevantItems = IterativeSimilarityAggregation
        .filterRelevantItems(relevanceScoreResult, threshold);
    int[] relevantItems = new int[] { 0, 1, 2, 3 };
    assertTrue(Arrays.equals(filterRelevantItems, relevantItems));
  }

  @Test
  public void testRanking() {
    int[] filterRelevantItems = IterativeSimilarityAggregation
        .filterRelevantItems(relevanceScoreResult, threshold);
    DenseDoubleVector similarityScores = IterativeSimilarityAggregation
        .computeRelevanceScore(filterRelevantItems, similarityMatrix);
    DoubleVector rankScores = IterativeSimilarityAggregation.rankScores(alpha,
        relevanceScoreResult, similarityScores);

    double sum = rankScores.subtract(rankingResult).sum();
    assertTrue(sum >= 0.0d);
    assertTrue(sum < 0.001d);
  }

  @Test
  public void testGetTopRankedItems() {
    DenseDoubleVector rankedTokens = new DenseDoubleVector(new double[] { 0.9,
        0.8, 0.2, 0.3, 0.75 });
    int[] topRankedItems = IterativeSimilarityAggregation.getTopRankedItems(
        rankedTokens, 0.7);
    assertEquals(3, topRankedItems.length);
    assertTrue(Arrays.equals(new int[] { 0, 1, 4 }, topRankedItems));
  }

  @Test
  public void testStaticThresholding() {

    // rows are the terms, columns are the contexts
    DenseDoubleMatrix weights = new DenseDoubleMatrix(new double[][] {
        { 0.2, 0.8, 0.9 }, // A
        { 0.1, 0.8, 0.7 }, // B
        { 0.1, 0.88, 0.79 }, // C
        { 0.45, 0.6, 0.4 }, // D
        { 0.89, 0.4, 0.1 }, // E
        { 0.88, 0.4, 0.1 } // F
        });
    Tuple3<String[], String[], DenseDoubleMatrix> bipartiteGraph = new Tuple3<String[], String[], DenseDoubleMatrix>(
        terms, new String[] { "context A", "context B", "context C" }, weights);

    IterativeSimilarityAggregation aggregation = new IterativeSimilarityAggregation(
        seedStringSet, bipartiteGraph);
    aggregation.init();
    String[] expandedset = aggregation.startStaticThresholding(0.7, 0);
    // should be just [A B C D]
    assertTrue(Arrays.equals(new String[] { "A", "B", "C", "D" }, expandedset));
  }

}
