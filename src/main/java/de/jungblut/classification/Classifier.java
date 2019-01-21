package de.jungblut.classification;

import de.jungblut.math.DoubleVector;

/**
 * Classifier interface for predicting categorial variables.
 * 
 * @author thomas.jungblut
 * 
 */
public interface Classifier extends Predictor {

  /**
   * Trains this classifier with the given features and the outcome.
   * 
   * @param outcome the outcome must have classes labeled as doubles. E.G. in
   *          the binary case you have a single element and decide between 0d
   *          and 1d. In higher dimensional cases you have each of these single
   *          elements mapped to a dimension.
   */
  public void train(DoubleVector[] features, DoubleVector[] outcome);

  /**
   * Trains this classifier with the given features and the outcome. This is the
   * streaming method for training, it takes parallel iterables.
   * 
   * @param outcome the outcome must have classes labeled as doubles. E.G. in
   *          the binary case you have a single element and decide between 0d
   *          and 1d. In higher dimensional cases you have each of these single
   *          elements mapped to a dimension.
   */
  public void train(Iterable<DoubleVector> features,
      Iterable<DoubleVector> outcome);

}
