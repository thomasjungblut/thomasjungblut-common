package de.jungblut.online.ml;

import de.jungblut.math.DoubleVector;

public class FeatureOutcomePair {

    private final DoubleVector feature;
    private final DoubleVector outcome;

    public FeatureOutcomePair(DoubleVector feature, DoubleVector outcome) {
        this.feature = feature;
        this.outcome = outcome;
    }

    public DoubleVector getFeature() {
        return this.feature;
    }

    public DoubleVector getOutcome() {
        return this.outcome;
    }

    @Override
    public String toString() {
        return "[feature=" + this.feature + ", outcome=" + this.outcome + "]";
    }

}
