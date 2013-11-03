package de.jungblut.classification;

import java.util.Arrays;

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
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    train(Arrays.asList(features), Arrays.asList(outcome));
  }

  @Override
  public void train(Iterable<DoubleVector> features,
      Iterable<DoubleVector> outcome) {
    throw new UnsupportedOperationException("Not yet implemented!");
  }

  @Override
  public int predictedClass(DoubleVector features, double threshold) {
    DoubleVector predict = predict(features);
    return extractPredictedClass(predict, threshold);
  }

  @Override
  public int predictedClass(DoubleVector features) {
    DoubleVector predict = predict(features);
    return extractPredictedClass(predict);
  }

  @Override
  public DoubleVector predictProbability(DoubleVector features) {
    DoubleVector predict = predict(features);
    return predict.divide(predict.sum());
  }

  @Override
  public int extractPredictedClass(DoubleVector predict) {
    if (predict.getLength() == 1) {
      return (int) Math.rint(predict.get(0));
    } else {
      return ArrayUtils.maxIndex(predict.toArray());
    }
  }

  @Override
  public int extractPredictedClass(DoubleVector predict, double threshold) {
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
