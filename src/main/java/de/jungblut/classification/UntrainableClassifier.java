package de.jungblut.classification;

import com.google.common.base.Preconditions;
import de.jungblut.math.DoubleVector;

public class UntrainableClassifier implements Classifier {

    private final Predictor predictor;

    public UntrainableClassifier(Predictor predictor) {
        this.predictor = Preconditions.checkNotNull(predictor);
    }

    @Override
    public DoubleVector predict(DoubleVector features) {
        return predictor.predict(features);
    }

    @Override
    public DoubleVector predictProbability(DoubleVector features) {
        return predictor.predictProbability(features);
    }

    @Override
    public int predictedClass(DoubleVector features, double threshold) {
        return predictor.predictedClass(features, threshold);
    }

    @Override
    public int predictedClass(DoubleVector features) {
        return predictor.predictedClass(features);
    }

    @Override
    public int extractPredictedClass(DoubleVector predict) {
        return predictor.extractPredictedClass(predict);
    }

    @Override
    public int extractPredictedClass(DoubleVector predict, double threshold) {
        return predictor.extractPredictedClass(predict, threshold);
    }

    @Override
    public void train(Iterable<DoubleVector> features,
                      Iterable<DoubleVector> outcome) {
        throw new UnsupportedOperationException("Not designed to train anything.");
    }

    @Override
    public void train(DoubleVector[] features, DoubleVector[] outcome) {
        throw new UnsupportedOperationException("Not designed to train anything.");
    }

}
