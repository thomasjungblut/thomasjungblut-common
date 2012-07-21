package de.jungblut.ner;

import gnu.trove.list.array.TIntArrayList;
import de.jungblut.distance.CosineDistance;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.distance.SimilarityMeasurer;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple3;

/**
 * Iterative similarity aggregation for named entity recognition and set
 * expansion based on the paper
 * "SEISA: Set Expansion by Iterative Similarity Aggregation". Those who wonder
 * what the package name "ner" stands for, it is "named entity recognition".
 * 
 * @author thomas.jungblut
 * 
 */
public final class IterativeSimilarityAggregation {

  private final String[] seedTokens;
  private final Tuple3<String[], String[], DenseDoubleMatrix> bipartiteGraph;
  private final double alpha;
  private final SimilarityMeasurer similarityMeasurer;

  /**
   * Constructs the similarity aggregation by seed tokens to expand and a given
   * bipartite graph. The bipartite graph is represented as a three tuple, which
   * consists of the vertices (called (candidate-) terms or entities) on the
   * first item, the context vertices on the second item and the edges between
   * those is a NxM matrix, where n is the entity tokens count and m is the
   * number of the context vertices. Alpha is set to 0.5 and the cosine distance
   * is used.
   */
  public IterativeSimilarityAggregation(String[] seedTokens,
      Tuple3<String[], String[], DenseDoubleMatrix> bipartiteGraph) {
    this(seedTokens, bipartiteGraph, 0.5d, new CosineDistance());
  }

  /**
   * Constructs the similarity aggregation by seed tokens to expand and a given
   * bipartite graph. The bipartite graph is represented as a three tuple, which
   * consists of the vertices (called (candidate-) terms or entities) on the
   * first item, the context vertices on the second item and the edges between
   * those is a NxM matrix, where n is the entity tokens count and m is the
   * number of the context vertices. Alpha is the constant weighting factor used
   * throughout the paper (usually 0.5). The distance measurer to be used must
   * be also defined.
   */
  public IterativeSimilarityAggregation(String[] seedTokens,
      Tuple3<String[], String[], DenseDoubleMatrix> bipartiteGraph,
      double alpha, DistanceMeasurer distance) {
    this.seedTokens = seedTokens;
    this.bipartiteGraph = bipartiteGraph;
    this.alpha = alpha;
    this.similarityMeasurer = new SimilarityMeasurer(distance);
  }

  /**
   * Starts the static thresholding algorithm and returns the expandedset of
   * newly found related tokens.
   */
  public String[] startStaticThresholding(double similarityThreshold) {
    return seedTokens;
  }

  /**
   * Computes the relevance for each term in U (universe of entities) to the
   * terms in the seedset.
   * 
   * @param seedSet S a subset of U, this are the indices where to find the
   *          items in the similarity matrix.
   * @param similarityMatrix the pairwise similarity matrix of the entities.
   * @return a vector of length of the universe of entities. Which index
   *         encapsulates the relevance described in the paper as
   *         S_rel(TERM_AT_INDEX_i,S)
   */
  static DenseDoubleVector computeRelevanceScore(int[] seedSet,
      DenseDoubleMatrix similarityMatrix) {
    final int termsLength = similarityMatrix.getColumnCount();
    final DenseDoubleVector relevanceScores = new DenseDoubleVector(termsLength);

    // TODO in the paper there is a multiplication with the number of terms, in
    // the example there is nothing
    final double constantLoss = 1.0d / seedSet.length;

    for (int i = 0; i < termsLength; i++) {
      double sum = 0.0d;
      for (int j : seedSet) {
        sum += similarityMatrix.get(j, i);
      }
      relevanceScores.set(i, constantLoss * sum);
    }

    return relevanceScores;
  }

  /**
   * Ranks the terms at the indices by their relevance scores and the similarity
   * scores. They are multiplied by the given alpha.
   * 
   * @return a vector which represents the rank of the terms.
   */
  static DoubleVector rankScores(double alpha,
      DenseDoubleVector relevanceScores, DenseDoubleVector similarityScores) {
    DoubleVector multiply = relevanceScores.multiply(alpha);
    return similarityScores.multiply(alpha).add(multiply);
  }

  /**
   * Returns the indices of the relevant items that are above the threshold.
   */
  static int[] filterRelevantItems(DenseDoubleVector relevanceScores,
      double threshold) {
    TIntArrayList list = new TIntArrayList();

    for (int i = 0; i < relevanceScores.getLength(); i++) {
      double val = relevanceScores.get(i);
      if (val > threshold) {
        list.add(i);
      }
    }
    return list.toArray();
  }

}
