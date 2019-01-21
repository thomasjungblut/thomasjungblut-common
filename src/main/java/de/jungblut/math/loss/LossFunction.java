package de.jungblut.math.loss;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;

/**
 * Calculates the error, for example in the last layer of a neural net.
 * 
 * @author thomas.jungblut
 * 
 */
public interface LossFunction {

  /**
   * Calculate the error with the given parameters.
   * 
   * @param y the real outcome as a matrix- rows contain the examples, columns
   *          the examples' output.
   * @param hypothesis the hypothesis as a matrix- rows contain the examples,
   *          columns the predicted output.
   * @return a positive value that denotes the error between the two matrices.
   */
  public double calculateLoss(DoubleMatrix y, DoubleMatrix hypothesis);

  /**
   * Calculate the error with the given parameters.
   * 
   * @param y the real outcome as a vector single example.
   * @param hypothesis the hypothesis as a vector single example.
   * @return a positive value that denotes the error between the two vectors.
   */
  public double calculateLoss(DoubleVector y, DoubleVector hypothesis);

  /**
   * Calculate the gradient with the given parameters.
   * 
   * @param y the real outcome as a vector single example.
   * @param hypothesis the hypothesis as a vector single example.
   * @return a vector that denotes the gradient given the hypothesis and real
   *         outcome.
   */
  public DoubleVector calculateGradient(DoubleVector feature, DoubleVector y,
      DoubleVector hypothesis);

}
