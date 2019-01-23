package de.jungblut.classification.knn;

import de.jungblut.datastructure.DistanceResult;
import de.jungblut.datastructure.InvertedIndex;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.jrpt.VectorDistanceTuple;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.named.KeyedDoubleVector;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * K nearest neighbour classification algorithm that is seeded with a "database"
 * of known examples and predicts based on the k-nearest neighbours majority
 * vote for a class. An Inverted Index is used internally to speedup the
 * searches.<br/>
 * TODO maybe we can add a sampling facility for larger data.
 */
public final class SparseKNearestNeighbours extends AbstractKNearestNeighbours {

    private final InvertedIndex<DoubleVector, Integer> index;
    private final TIntObjectHashMap<DoubleVector> featureOutcomeMap = new TIntObjectHashMap<>();

    /**
     * Constructs a new knn classifier.
     *
     * @param numOutcomes the number of different outcomes that can be predicted.
     * @param k           the number of neighbours to analyse to get a prediction (it does
     *                    so by majority voting).
     * @param measurer    the distance measurer to use.
     */
    public SparseKNearestNeighbours(int numOutcomes, int k,
                                    DistanceMeasurer measurer) {
        super(numOutcomes, k);
        this.index = InvertedIndex.createVectorIndex(measurer);
    }

    @Override
    public void train(Iterable<DoubleVector> features,
                      Iterable<DoubleVector> outcome) {

        List<DoubleVector> featureList = new ArrayList<>();
        Iterator<DoubleVector> featIterator = features.iterator();
        Iterator<DoubleVector> outIterator = outcome.iterator();

        int id = 0;
        while (featIterator.hasNext()) {
            featureList.add(new KeyedDoubleVector(id, featIterator.next()));
            featureOutcomeMap.put(id, outIterator.next());
            id++;
        }
        index.build(featureList);
    }

    @Override
    protected List<VectorDistanceTuple<DoubleVector>> getNearestNeighbours(
            DoubleVector feature, int k) {
        List<VectorDistanceTuple<DoubleVector>> neighbours = new ArrayList<>();
        List<DistanceResult<DoubleVector>> result = index.query(feature, k,
                Double.MAX_VALUE);
        // now we need to join the features with its outcome
        for (DistanceResult<DoubleVector> res : result) {
            KeyedDoubleVector resValue = (KeyedDoubleVector) res.get();
            neighbours.add(new VectorDistanceTuple<>(res.get(), featureOutcomeMap
                    .get(resValue.getKey()), res.getDistance()));
        }
        return neighbours;
    }
}
