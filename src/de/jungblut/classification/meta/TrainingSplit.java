package de.jungblut.classification.meta;

import de.jungblut.math.DoubleVector;

public class TrainingSplit {

  private final DoubleVector[] trainFeatures;
  private final DoubleVector[] trainOutcome;

  /**
   * Sets the split internally.
   */
  public TrainingSplit(DoubleVector[] trainFeatures, DoubleVector[] trainOutcome) {
    this.trainFeatures = trainFeatures;
    this.trainOutcome = trainOutcome;
  }

  public DoubleVector[] getTrainFeatures() {
    return this.trainFeatures;
  }

  public DoubleVector[] getTrainOutcome() {
    return this.trainOutcome;
  }

}
