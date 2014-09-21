package de.jungblut.classification;

import de.jungblut.math.DoubleVector;

public interface Predictor {

  /**
   * Classifies the given features.
   * 
   * @return the vector that contains an indicator at the index of the class.
   *         Usually zero or 1, in some cases it is a probability or activation
   *         value.
   */
  public DoubleVector predict(DoubleVector features);

  /**
   * Classifies the given features.
   * 
   * @return a vector that returns the probability of all outcomes. The output
   *         vector should sum to one.
   */
  public DoubleVector predictProbability(DoubleVector features);

  /**
   * Classifies the given features.
   * 
   * @param threshold the threshold for the prediction "probability". In the
   *          sigmoid and binary case, you want to set everything greater (>)
   *          0.5 to 1d and everything below (<=) to 0d.
   * @return the predicted class as an integer for the output of a classifier.
   */
  public int predictedClass(DoubleVector features, double threshold);

  /**
   * Classifies the given features.
   * 
   * @return the predicted class as an integer for the output of a classifier.
   */
  public int predictedClass(DoubleVector features);

  /**
   * Given an already done prediction, choose the class.
   * 
   * @return the class index as integer.
   */
  public int extractPredictedClass(DoubleVector predict);

  /**
   * Given an already done prediction, choose the class with a threshold.
   * 
   * @return the class index as integer.
   */
  public int extractPredictedClass(DoubleVector predict, double threshold);
}
