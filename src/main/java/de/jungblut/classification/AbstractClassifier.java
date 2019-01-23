package de.jungblut.classification;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import de.jungblut.math.DoubleVector;

import java.util.Arrays;

/**
 * Abstract base class for classifiers.
 *
 * @author thomas.jungblut
 */
public abstract class AbstractClassifier extends AbstractPredictor implements
        Classifier {

    @Override
    public void train(DoubleVector[] features, DoubleVector[] outcome) {
        Preconditions.checkArgument(features.length > 0,
                "Features must contain at least a single item!");
        Preconditions.checkArgument(features.length == outcome.length,
                "There must be an equal amount of features and prediction outcomes!");
        train(Arrays.asList(features), Arrays.asList(outcome));
    }

    @Override
    public void train(Iterable<DoubleVector> features,
                      Iterable<DoubleVector> outcome) {
        train(Iterables.toArray(features, DoubleVector.class),
                Iterables.toArray(outcome, DoubleVector.class));
    }
}
