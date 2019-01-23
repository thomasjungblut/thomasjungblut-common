package de.jungblut.math.minimize;

import de.jungblut.math.DoubleVector;

/**
 * Callback that should be triggered when a iteration was finished.
 *
 * @author thomas.jungblut
 */
public interface IterationCompletionListener {

    /**
     * This callback is called from a {@link AbstractMinimizer} when a iteration
     * of a minimization objective is finished.
     *
     * @param iteration      the number of the current iteration.
     * @param cost           the cost at the current iteration.
     * @param currentWeights the current optimal weights.
     */
    public void onIterationFinished(int iteration, double cost,
                                    DoubleVector currentWeights);

}
