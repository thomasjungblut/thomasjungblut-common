package de.jungblut.math.squashing;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;

/**
 * Calculates the error, for example in the last layer of a neural net.
 * 
 * @author thomas.jungblut
 * 
 */
public interface ErrorFunction {

  /**
   * Calculate the error with the given parameters.
   * 
   * @param y the real outcome as a matrix- rows contain the examples, columns
   *          the examples' output.
   * @param hypothesis the hypothesis as a matrix- rows contain the examples,
   *          columns the predicted output.
   * @return a positive value that denotes the error between the two matrices.
   */
  public double calculateError(DoubleMatrix y, DoubleMatrix hypothesis);

  /**
   * Calculate the error with the given parameters.
   * 
   * @param y the real outcome as a vector single example.
   * @param hypothesis the hypothesis as a vector single example.
   * @return a positive value that denotes the error between the two vectors.
   */
  public double calculateError(DoubleVector y, DoubleVector hypothesis);

  /**
   * Calculate the derivative with the given parameters.
   * 
   * @param y the real outcome as a vector single example.
   * @param hypothesis the hypothesis as a vector single example.
   * @return a vector that denotes the derivative given the hypothesis and real
   *         outcome.
   */
  public DoubleVector calculateDerivative(DoubleVector y,
      DoubleVector hypothesis);

}
