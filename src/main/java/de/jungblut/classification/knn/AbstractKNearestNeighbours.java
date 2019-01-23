package de.jungblut.classification.knn;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.jrpt.VectorDistanceTuple;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.SingleEntryDoubleVector;

import java.util.List;

/**
 * K nearest neighbour classification algorithm that is seeded with a "database"
 * of known examples and predicts based on the k-nearest neighbours majority
 * vote for a class.
 */
public abstract class AbstractKNearestNeighbours extends AbstractClassifier {

    protected final int numOutcomes;
    protected final int k;

    /**
     * Constructs a new knn classifier.
     *
     * @param numOutcomes the number of different outcomes that can be predicted.
     * @param k           the number of neighbours to analyse to get a prediction (it does
     *                    so by majority voting).
     */
    public AbstractKNearestNeighbours(int numOutcomes, int k) {
        this.numOutcomes = numOutcomes;
        this.k = k;
    }

    /**
     * @return If the number of outcomes is 2 (binary prediction) the returned
     * vector contains the class id (0 or 1) at the first index. If not, a
     * histogram of the classes that were predicted.
     */
    @Override
    public DoubleVector predict(DoubleVector features) {
        List<VectorDistanceTuple<DoubleVector>> nearestNeighbours = getNearestNeighbours(
                features, k);

        DenseDoubleVector outcomeHistogram = new DenseDoubleVector(numOutcomes);
        for (VectorDistanceTuple<DoubleVector> tuple : nearestNeighbours) {
            int classIndex = 0;
            if (numOutcomes == 2) {
                classIndex = (int) tuple.getValue().get(0);
            } else {
                classIndex = tuple.getValue().maxIndex();
            }

            outcomeHistogram.set(classIndex, outcomeHistogram.get(classIndex) + 1);
        }
        if (numOutcomes == 2) {
            return new SingleEntryDoubleVector(outcomeHistogram.maxIndex());
        } else {
            return outcomeHistogram;
        }
    }

    @Override
    public DoubleVector predictProbability(DoubleVector features) {
        DoubleVector prediction = predict(features);
        if (numOutcomes != 2) {
            prediction = prediction.divide(prediction.sum());
        }
        return prediction;
    }

    /**
     * Find the k nearest neighbours for the given feature.
     *
     * @param feature the feature to find neighbours for.
     * @param k       the number of neighbours to find.
     * @return a list of {@link VectorDistanceTuple}'s that contain the outcome of
     * the retrieved vectors.
     */
    protected abstract List<VectorDistanceTuple<DoubleVector>> getNearestNeighbours(
            DoubleVector feature, int k);

}
