package de.jungblut.math.minimize;

import java.util.ArrayList;
import java.util.List;

import de.jungblut.math.DoubleVector;

/**
 * Abstract minimizer class that adds functionality that can be shared between
 * many minimizers. Currently it has just a iteration completion callback
 * facility, but can be later extended to share line searching code or other
 * shared utilities.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class AbstractMinimizer implements Minimizer {

  private List<IterationCompletionListener> listenerList = new ArrayList<>();

  /**
   * Add a callback listener that triggers after a iteration.
   * 
   * @param lst the iteration completion listener.
   */
  public void addIterationCompletionCallback(IterationCompletionListener lst) {
    listenerList.add(lst);
  }

  /**
   * Callback method that should be called from an explicit subclass once an
   * iteration was finished.
   * 
   * @param iteration the number of the current iteration.
   * @param cost the cost at the current iteration.
   * @param currentWeights the current optimal weights.
   */
  protected final void onIterationFinished(int iteration, double cost,
      DoubleVector currentWeights) {
    for (IterationCompletionListener list : listenerList) {
      list.onIterationFinished(iteration, cost, currentWeights);
    }
  }

}
