package de.jungblut.ner;

import gnu.trove.list.array.TIntArrayList;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.ArrayUtils;
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

  private final double alpha;
  private final SimilarityMeasurer similarityMeasurer;
  private DenseDoubleMatrix similarityMatrix;

  private final String[] seedTokens;
  private int[] seedIndices;
  private String[] termNodes;
  private DenseDoubleMatrix weightMatrix;

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
    this.termNodes = bipartiteGraph.getFirst();
    this.weightMatrix = bipartiteGraph.getThird();
    this.alpha = alpha;
    this.similarityMeasurer = new SimilarityMeasurer(distance);
    init();
  }

  /**
   * Initializes the vectorized structures for algorithm use.
   */
  public void init() {

    // similarity between the term nodes are defined by the similarity of their
    // context in which they occur. So the context nodes are the feature of this
    // algorithm.
    similarityMatrix = new DenseDoubleMatrix(termNodes.length, termNodes.length);
    /*
     * In our case we are going to loop through the weight matrix and measuring
     * similary between the terms (represented as rows) via their columns
     * (features, or simply the occurances in the context).
     */
    for (int i = 0; i < termNodes.length; i++) {
      for (int j = 0; j < termNodes.length; j++) {
        if (i != j) {
          similarityMatrix.set(i, j, similarityMeasurer.measureSimilarity(
              weightMatrix.getRow(i), weightMatrix.getRow(j)));
        }
      }
    }

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
   */
  public String[] startStaticThresholding(double similarityThreshold,
      int maxIterations) {

    DenseDoubleVector relevanceScore = computeRelevanceScore(seedIndices,
        similarityMatrix);
    int[] relevantTokens = filterRelevantItems(relevanceScore,
        similarityThreshold);

    int iteration = 0;
    while (true) {

      DenseDoubleVector similarityScore = computeRelevanceScore(relevantTokens,
          similarityMatrix);
      DoubleVector rankedTokens = rankScores(alpha, relevanceScore,
          similarityScore);
      int[] topRankedItems = getTopRankedItems(rankedTokens, relevantTokens,
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
      
      // TODO merge the new relevant tokens together
//      relevantTokens = ArrayUtils.concat();

      if (equal || (maxIterations > 0 && iteration > maxIterations)) {
        break;
      }
      
      iteration++;
    }

    return seedTokens;
  }

  /**
   * Simple selection sort with filtering function. It actually mutates the
   * rankedTokens parameter, so please don't wonder about the side-effects. In
   * the algorithm the ranked tokens vector isn't used anymore later.
   */
  static int[] getTopRankedItems(DoubleVector rankedTokens,
      int[] relevantTokens, double similarityThreshold) {
    TIntArrayList sorted = new TIntArrayList();
    for (int item = 0; item < rankedTokens.getLength(); item++) {
      int maxIndex = item;
      for (int i = item; i < rankedTokens.getLength(); i++) {
        if (rankedTokens.get(i) > rankedTokens.get(maxIndex)) {
          maxIndex = i;
        }
      }
      // swap the values so we don't find our max again in the next iteration
      double tmp = rankedTokens.get(maxIndex);
      rankedTokens.set(maxIndex, rankedTokens.get(item));
      rankedTokens.set(item, tmp);
      if (tmp > similarityThreshold) {
        sorted.add(relevantTokens[maxIndex]);
      } else {
        break;
      }
    }
    return sorted.toArray();
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
