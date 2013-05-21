package de.jungblut.classification.knn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.DistanceResult;
import de.jungblut.datastructure.InvertedIndex;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * K nearest neighbour classification algorithm that is seeded with a "database"
 * of known examples and predicts based on the k-nearest neighbours majority
 * vote for a class. An Inverted Index is used internally to speedup the
 * searches.
 * 
 */
public final class SparseKNearestNeighbours extends AbstractKNearestNeighbours {

  private final InvertedIndex<DoubleVector, Integer> index;
  private final Map<DoubleVector, DoubleVector> featureOutcomeMap = new HashMap<>();

  /**
   * Constructs a new knn classifier.
   * 
   * @param numOutcomes the number of different outcomes that can be predicted.
   * @param k the number of neighbours to analyse to get a prediction (it does
   *          so by majority voting).
   * @param measurer the distance measurer to use.
   */
  public SparseKNearestNeighbours(int numOutcomes, int k,
      DistanceMeasurer measurer) {
    super(numOutcomes, k);
    this.index = InvertedIndex.createVectorIndex(measurer);
  }

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    Preconditions.checkArgument(features.length == outcome.length,
        "Features and outcome length didn't match: " + features.length + "!="
            + outcome.length);
    Preconditions.checkArgument(features.length > 0,
        "You need at least a single item in your classifier!");
    index.build(Arrays.asList(features));
    for (int i = 0; i < features.length; i++) {
      featureOutcomeMap.put(features[i], outcome[i]);
    }
  }

  @Override
  protected List<VectorDistanceTuple<DoubleVector>> getNearestNeighbours(
      DoubleVector feature, int k) {
    List<VectorDistanceTuple<DoubleVector>> neighbours = new ArrayList<>();
    List<DistanceResult<DoubleVector>> result = index.query(feature, k,
        Double.MAX_VALUE);
    // now we need to join the features with its outcome
    for (DistanceResult<DoubleVector> res : result) {
      neighbours.add(new VectorDistanceTuple<>(res.get(), featureOutcomeMap
          .get(res.get()), res.getDistance()));
    }
    return neighbours;
  }
}
