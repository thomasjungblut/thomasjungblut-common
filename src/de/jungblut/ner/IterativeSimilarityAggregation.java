package de.jungblut.ner;

import de.jungblut.distance.CosineDistance;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.distance.SimilarityMeasurer;
import de.jungblut.math.dense.DenseDoubleMatrix;
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
  private final double alpha;
  private final SimilarityMeasurer similarityMeasurer;
  private String[] termNodes;
  private String[] contextNodes;
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
    this.contextNodes = bipartiteGraph.getSecond();
    this.weightMatrix = bipartiteGraph.getThird();
    this.alpha = alpha;
    this.similarityMeasurer = new SimilarityMeasurer(distance);
  }

  /**
   * Starts the static thresholding algorithm and returns the
   */
  public String[] startStaticThresholding(double similarityThreshold) {
    return seedTokens;
  }

}
