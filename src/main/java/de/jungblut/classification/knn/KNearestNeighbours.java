package de.jungblut.classification.knn;

import com.codepoetics.protonpack.StreamUtils;
import de.jungblut.jrpt.KDTree;
import de.jungblut.jrpt.VectorDistanceTuple;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.tuple.Tuple;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * K nearest neighbour classification algorithm that is seeded with a "database"
 * of known examples and predicts based on the k-nearest neighbours majority
 * vote for a class. A KD tree is used internally to speedup the searches, thus
 * the distance metric is restricted to the EuclidianDistance. <br/>
 * TODO maybe we can add a sampling facility for larger data.
 */
public final class KNearestNeighbours extends AbstractKNearestNeighbours {

    private final KDTree<DoubleVector> tree;

    /**
     * Constructs a new knn classifier.
     *
     * @param numOutcomes the number of different outcomes that can be predicted.
     * @param k           the number of neighbours to analyse to get a prediction (it does
     *                    so by majority voting).
     */
    public KNearestNeighbours(int numOutcomes, int k) {
        super(numOutcomes, k);
        this.tree = new KDTree<>();
    }

    @Override
    public void train(Iterable<DoubleVector> features,
                      Iterable<DoubleVector> outcome) {

        // zip the streams and construct the kd tree
        Stream<Tuple<DoubleVector, DoubleVector>> stream = StreamUtils.zip(
                StreamSupport.stream(features.spliterator(), false),
                StreamSupport.stream(outcome.spliterator(), false),
                (l, r) -> new Tuple<>(l, r));

        tree.constructWithPayload(stream);
    }

    @Override
    protected List<VectorDistanceTuple<DoubleVector>> getNearestNeighbours(
            DoubleVector feature, int k) {
        return tree.getNearestNeighbours(feature, k);
    }
}
