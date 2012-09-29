package de.jungblut.classification.knn;

import java.util.List;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * K nearest neighbour classification algorithm that is seeded with a "database"
 * of known examples and predicts based on the k-nearest neighbours majority
 * vote for a class. A KD tree is used internally to speedup the searches.
 * 
 */
public final class KNearestNeighbours {

  private final KDTree<DoubleVector> tree;
  private final DistanceMeasurer measurer;
  private int numOutcomes;

  /**
   * Constructs a knn classifier with an array of feature vectors and outcomes.
   * Of course you need to pass a distance measurement to find the nearest
   * neighbours.<br/>
   * The outcome vectors need to be a one dimensional outcome and contain
   * classes as nominal values, e.g. 0 for false and 1 for true in a binary
   * case.
   * 
   * @param numOutcomes the number of different outcomes that can be predicted.
   */
  public KNearestNeighbours(DoubleVector[] features, DoubleVector[] outcome,
      DistanceMeasurer measurer, int numOutcomes) {
    this.measurer = measurer;
    this.numOutcomes = numOutcomes;
    Preconditions.checkArgument(features.length == outcome.length,
        "Features and outcome length didn't match: " + features.length + "!="
            + outcome.length);
    Preconditions.checkArgument(features.length > 0,
        "You need at least a single item in your classifier!");
    tree = new KDTree<>();
    for (int i = 0; i < features.length; i++) {
      tree.add(features[i], outcome[i]);
    }
  }

  /**
   * Predicts the outcome of the given feature vector by majority voting of
   * nearest k neighbours.
   * 
   * @return the majority voted class.
   */
  public int predict(DoubleVector vector, int k) {
    List<VectorDistanceTuple<DoubleVector>> nearestNeighbours = tree
        .getNearestNeighbours(vector, k, measurer);

    int[] outcomeHistogram = new int[numOutcomes];
    for (VectorDistanceTuple<DoubleVector> tuple : nearestNeighbours) {
      outcomeHistogram[(int) tuple.getValue().get(0)]++;
    }
    return ArrayUtils.maxIndex(outcomeHistogram);
  }
}
