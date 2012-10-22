package de.jungblut.classification;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;

/**
 * Abstract base class for classifiers.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class AbstractClassifier implements Classifier {

  @Override
  public int getPredictedClass(DoubleVector features) {
    DoubleVector predict = predict(features);
    return ArrayUtils.maxIndex(predict.toArray());
  }

}
