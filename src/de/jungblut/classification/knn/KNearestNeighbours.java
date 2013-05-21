package de.jungblut.classification.knn;

import java.util.List;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.math.DoubleVector;

/**
 * K nearest neighbour classification algorithm that is seeded with a "database"
 * of known examples and predicts based on the k-nearest neighbours majority
 * vote for a class. A KD tree is used internally to speedup the searches, thus
 * the distance metric is restricted to the EuclidianDistance.
 * 
 */
public final class KNearestNeighbours extends AbstractKNearestNeighbours {

  private final KDTree<DoubleVector> tree;

  /**
   * Constructs a new knn classifier.
   * 
   * @param numOutcomes the number of different outcomes that can be predicted.
   * @param k the number of neighbours to analyse to get a prediction (it does
   *          so by majority voting).
   */
  public KNearestNeighbours(int numOutcomes, int k) {
    super(numOutcomes, k);
    this.tree = new KDTree<>();
  }

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    Preconditions.checkArgument(features.length == outcome.length,
        "Features and outcome length didn't match: " + features.length + "!="
            + outcome.length);
    Preconditions.checkArgument(features.length > 0,
        "You need at least a single item in your classifier!");

    for (int i = 0; i < features.length; i++) {
      tree.add(features[i], outcome[i]);
    }
  }

  @Override
  protected List<VectorDistanceTuple<DoubleVector>> getNearestNeighbours(
      DoubleVector feature, int k) {
    return tree.getNearestNeighbours(feature, k);
  }
}
