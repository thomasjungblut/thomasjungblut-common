package de.jungblut.classification;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;

public abstract class AbstractPredictor implements Predictor {

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
