package de.jungblut.ner;

import gnu.trove.list.array.TIntArrayList;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.distance.CosineDistance;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.distance.SimilarityMeasurer;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

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

  private final double alpha;
  // similarity between the term nodes are defined by the similarity of their
  // context in which they occur. So the context nodes are the feature of this
  // algorithm
  private final SimilarityMeasurer similarityMeasurer;
  private final String[] seedTokens;

  private int[] seedIndices;
  private String[] termNodes;
  private DoubleMatrix weightMatrix;

  /**
   * Constructs the similarity aggregation by seed tokens to expand and a given
   * bipartite graph. The bipartite graph is represented as a two tuple, which
   * consists of the vertices (called (candidate-) terms or entities) on the
   * first item and the edges between those is a NxM matrix, where n is the
   * entity tokens count and m is the number of the context vertices. Alpha is
   * set to 0.5 and the cosine distance is used.
   */
  public IterativeSimilarityAggregation(String[] seedTokens,
      Tuple<String[], DoubleMatrix> bipartiteGraph) {
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
      Tuple<String[], DoubleMatrix> bipartiteGraph, double alpha,
      DistanceMeasurer distance) {
    this.seedTokens = seedTokens;
    this.termNodes = bipartiteGraph.getFirst();
    this.weightMatrix = bipartiteGraph.getSecond();
    this.alpha = alpha;
    this.similarityMeasurer = new SimilarityMeasurer(distance);
    init();
  }

  /**
   * Initializes the vectorized structures for algorithm use.
   */
  private void init() {
    seedIndices = new int[seedTokens.length];
    // the seed tokens must be defined in the term nodes to make this work
    for (int i = 0; i < seedTokens.length; i++) {
      String token = seedTokens[i];
      int find = ArrayUtils.find(termNodes, token);
      Preconditions.checkArgument(find >= 0, "Seed token \"" + token
          + "\" could not be found in the term list!");
      seedIndices[i] = find;
    }
  }

  /**
   * Starts the static thresholding algorithm and returns the expandedset of
   * newly found related tokens.
   * 
   * @param maxIterations if > 0 the algorithm will stop after reached
   *          maxIterations.
   */
  public String[] startStaticThresholding(double similarityThreshold,
      int maxIterations, boolean verbose) {

    DenseDoubleVector relevanceScore = computeRelevanceScore(seedIndices);
    int[] relevantTokens = filterRelevantItems(relevanceScore,
        similarityThreshold);

    int iteration = 0;
    while (true) {

      DenseDoubleVector similarityScore = computeRelevanceScore(relevantTokens);
      DoubleVector rankedTokens = rankScores(alpha, relevanceScore,
          similarityScore);
      int[] topRankedItems = getTopRankedItems(rankedTokens,
          similarityThreshold);

      // check the tokens for equality, order is important, their score is not
      boolean equal = relevantTokens.length == topRankedItems.length;
      if (equal) {
        for (int i = 0; i < topRankedItems.length; i++) {
          if (topRankedItems[i] != relevantTokens[i]) {
            equal = false;
            break;
          }
        }
      }

      if (verbose) {
        System.out.println(iteration + " | Top ranked item size: "
            + topRankedItems.length);
      }

      // simply exchange the old items with the newly found ones
      relevantTokens = topRankedItems;
      // break algorithm if tokens haven't changed or maxiterations have been
      // reached
      if (equal || (maxIterations > 0 && iteration > maxIterations)) {
        break;
      }

      iteration++;
    }

    String[] tokens = new String[relevantTokens.length];
    // translate the indices back to the tokens
    for (int i = 0; i < relevantTokens.length; i++) {
      tokens[i] = termNodes[relevantTokens[i]];
    }

    return tokens;
  }

  /**
   * Simple selection sort with filtering function. Can be optimized with a less
   * naive algorithm. Currently this is O(n^2+2n) which is really bad.
   */
  static int[] getTopRankedItems(DoubleVector pRankedTokens,
      double similarityThreshold) {
    DoubleVector rankedTokens = pRankedTokens.deepCopy();
    int[] sortedIndices = new int[rankedTokens.getLength()];
    for (int i = 0; i < sortedIndices.length; i++) {
      sortedIndices[i] = i;
    }

    for (int j = 0; j < rankedTokens.getLength() - 1; j++) {
      int max = j;
      for (int i = j + 1; i < rankedTokens.getLength(); i++) {
        if (rankedTokens.get(i) > rankedTokens.get(max)) {
          max = i;
        }
      }
      if (j != max) {
        double tmp = rankedTokens.get(max);
        rankedTokens.set(max, rankedTokens.get(j));
        rankedTokens.set(j, tmp);
        ArrayUtils.swap(sortedIndices, max, j);
      }
    }

    // filter these tokens
    TIntArrayList list = new TIntArrayList();
    for (int i = 0; i < sortedIndices.length; i++) {
      final double val = pRankedTokens.get(sortedIndices[i]);
      if (val > similarityThreshold) {
        list.add(sortedIndices[i]);
      } else {
        break;
      }
    }

    return list.toArray();
  }

  /**
   * Computes the relevance for each term in U (universe of entities) to the
   * terms in the seedset.
   * 
   * @param seedSet S a subset of U, this are the indices where to find the
   *          items in the similarity matrix.
   * @param weightmatrix of the given bipartite graph
   * @param termsLength the number of terms on the left side of the graph.
   * @return a vector of length of the universe of entities. Which index
   *         encapsulates the relevance described in the paper as
   *         S_rel(TERM_AT_INDEX_i,S)
   */
  private DenseDoubleVector computeRelevanceScore(int[] seedSet) {
    final int termsLength = termNodes.length;
    final DenseDoubleVector relevanceScores = new DenseDoubleVector(termsLength);

    final double constantLoss = 1.0d / seedSet.length;

    for (int i = 0; i < termsLength; i++) {
      double sum = 0.0d;
      for (int j : seedSet) {
        double similarity = similarityMeasurer.measureSimilarity(
            weightMatrix.getRowVector(i), weightMatrix.getRowVector(j));
        sum += similarity;
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
