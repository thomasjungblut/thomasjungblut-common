package de.jungblut.math.function;

import de.jungblut.math.DoubleVector;

/**
 * A function that can be applied to a double vector via {@link DoubleVector}
 * #apply({@link DoubleVectorFunction} f);
 * 
 * @author thomas.jungblut
 * 
 */
public interface DoubleVectorFunction {

  public double calculate(int index, double value);

}
