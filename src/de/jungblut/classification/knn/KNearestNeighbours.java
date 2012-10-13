package de.jungblut.classification.knn;

import java.util.List;

import com.google.common.base.Preconditions;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * K nearest neighbour classification algorithm that is seeded with a "database"
 * of known examples and predicts based on the k-nearest neighbours majority
 * vote for a class. A KD tree is used internally to speedup the searches.
 * 
 */
public final class KNearestNeighbours extends AbstractClassifier {

  private final KDTree<DenseDoubleVector> tree;
  private final DistanceMeasurer measurer;
  private final int numOutcomes;
  private final int k;

  /**
   * Constructs a new knn classifier. Of course you need to pass a distance
   * measurement to find the nearest neighbours.
   * 
   * @param numOutcomes the number of different outcomes that can be predicted.
   * @param k the number of neighbours to analyse to get a prediction (it does
   *          so by majority voting).
   */
  public KNearestNeighbours(DistanceMeasurer measurer, int numOutcomes, int k) {
    this.measurer = measurer;
    this.numOutcomes = numOutcomes;
    this.k = k;
    this.tree = new KDTree<>();
  }

  @Override
  public void train(DoubleVector[] features, DenseDoubleVector[] outcome) {
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
  public DoubleVector predict(DoubleVector features) {
    List<VectorDistanceTuple<DenseDoubleVector>> nearestNeighbours = tree
        .getNearestNeighbours(features, k, measurer);

    DoubleVector outcomeHistogram = new DenseDoubleVector(numOutcomes);
    for (VectorDistanceTuple<DenseDoubleVector> tuple : nearestNeighbours) {
      int classIndex = 0;
      if (numOutcomes == 2) {
        classIndex = (int) tuple.getValue().get(0);
      } else {
        classIndex = tuple.getValue().maxIndex();
      }

      outcomeHistogram.set(classIndex, outcomeHistogram.get(classIndex) + 1);
    }
    return outcomeHistogram;
  }
}
