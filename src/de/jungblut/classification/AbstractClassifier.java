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
  public int getPredictedClass(DoubleVector features, double threshold) {
    DoubleVector predict = predict(features);
    return predictClassInternal(predict, threshold);
  }

  @Override
  public int getPredictedClass(DoubleVector features) {
    DoubleVector predict = predict(features);
    return predictClassInternal(predict);
  }

  @Override
  public int predictClassInternal(DoubleVector predict) {
    if (predict.getLength() == 1) {
      return (int) Math.rint(predict.get(0));
    } else {
      return ArrayUtils.maxIndex(predict.toArray());
    }
  }

  @Override
  public int predictClassInternal(DoubleVector predict, double threshold) {
    if (predict.getLength() == 1) {
      if (predict.get(0) <= threshold) {
        return 0;
      } else {
        return 1;
      }
    } else {
      return ArrayUtils.maxIndex(predict.toArray());
    }
  }
}
