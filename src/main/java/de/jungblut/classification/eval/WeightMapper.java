package de.jungblut.classification.eval;

import de.jungblut.classification.Classifier;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.CostFunction;

/**
 * This interface helps to map minimizable weights of a {@link CostFunction} to
 * a {@link Classifier} implementation.
 *
 * @author thomas.jungblut
 */
public interface WeightMapper<A extends Classifier> {

    /**
     * Maps the given weights to a classifier implementation.
     *
     * @param weights the parameters of a trained model.
     * @return a classifier instance with the weights set.
     */
    public A mapWeights(DoubleVector weights);

}
