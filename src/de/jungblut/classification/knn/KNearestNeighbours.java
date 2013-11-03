package de.jungblut.classification.knn;

import java.util.Iterator;
import java.util.List;

import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.math.DoubleVector;

/**
 * K nearest neighbour classification algorithm that is seeded with a "database"
 * of known examples and predicts based on the k-nearest neighbours majority
 * vote for a class. A KD tree is used internally to speedup the searches, thus
 * the distance metric is restricted to the EuclidianDistance. <br/>
 * TODO maybe we can add a sampling facility for larger data.
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
  public void train(Iterable<DoubleVector> features,
      Iterable<DoubleVector> outcome) {

    Iterator<DoubleVector> featIterator = features.iterator();
    Iterator<DoubleVector> outIterator = outcome.iterator();

    while (featIterator.hasNext()) {
      tree.add(featIterator.next(), outIterator.next());
    }

  }

  @Override
  protected List<VectorDistanceTuple<DoubleVector>> getNearestNeighbours(
      DoubleVector feature, int k) {
    return tree.getNearestNeighbours(feature, k);
  }
}
