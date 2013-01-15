package de.jungblut.classification.bayes;

import java.util.Iterator;

import com.google.common.base.Preconditions;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.DenseIntVector;

/**
 * Simple multinomial naive bayes classifier.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MultinomialNaiveBayesClassifier extends AbstractClassifier {

  private DenseDoubleMatrix probabilityMatrix;
  private DenseDoubleVector classProbability;

  /**
   * Default constructor to construct this classifier.
   */
  public MultinomialNaiveBayesClassifier() {
  }

  /**
   * Deserialization constructor to instantiate an already trained classifier
   * from the internal representations.
   * 
   * @param probabilityMatrix the probability matrix.
   * @param classProbability the prior class probabilities.
   */
  public MultinomialNaiveBayesClassifier(DenseDoubleMatrix probabilityMatrix,
      DenseDoubleVector classProbability) {
    super();
    this.probabilityMatrix = probabilityMatrix;
    this.classProbability = classProbability;
  }

  @Override
  public void train(DoubleVector[] features, DenseDoubleVector[] outcome) {
    int[] classes = new int[outcome.length];
    for (int i = 0; i < outcome.length; i++) {
      if (outcome[i].getDimension() == 1) {
        classes[i] = (int) outcome[i].get(0);
      } else {
        classes[i] = outcome[i].maxIndex();
      }
    }
    trainInternal(features, new DenseIntVector(classes));
  }

  /**
   * Trains this classifier by the given word counts and the prediction.
   */
  private void trainInternal(DoubleVector[] features, DenseIntVector prediction) {
    Preconditions.checkArgument(features.length > 0,
        "Features must contain at least a single item!");
    Preconditions.checkArgument(features.length == prediction.getLength(),
        "There must be an equal amount of features and prediction outcomes!");

    final int numDistinctClasses = prediction.getNumberOfDistinctElements();
    probabilityMatrix = new DenseDoubleMatrix(numDistinctClasses,
        features[0].getDimension(), 1.0d);

    int[] tokenPerClass = new int[numDistinctClasses];
    int[] numDocumentsPerClass = new int[numDistinctClasses];

    // init the probability with the document length = word count for each token
    for (int columnIndex = 0; columnIndex < features.length; columnIndex++) {
      final DoubleVector document = features[columnIndex];
      final int predictedClass = prediction.get(columnIndex);
      tokenPerClass[predictedClass] += document.getLength();
      numDocumentsPerClass[predictedClass]++;

      Iterator<DoubleVectorElement> iterateNonZero = document.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        double currentCount = probabilityMatrix.get(predictedClass,
            next.getIndex());
        probabilityMatrix.set(predictedClass, next.getIndex(), currentCount
            + next.getValue());
      }
    }

    // know we know the token distribution per class, we can calculate the
    // probability. It is intended for them to be negative in some cases
    for (int row = 0; row < numDistinctClasses; row++) {
      for (int tokenColumn = 0; tokenColumn < probabilityMatrix
          .getColumnCount(); tokenColumn++) {
        double currentWordCount = probabilityMatrix.get(row, tokenColumn);
        double logLikelyhood = Math.log(currentWordCount
            / (tokenPerClass[row] + probabilityMatrix.getColumnCount() - 1));
        probabilityMatrix.set(row, tokenColumn, logLikelyhood);
      }
    }

    classProbability = new DenseDoubleVector(numDistinctClasses);
    for (int i = 0; i < numDistinctClasses; i++) {
      classProbability.set(i, (numDocumentsPerClass[i])
          / (double) features.length);
    }
  }

  /**
   * Returns the maximum likely class.
   */
  public int classify(DoubleVector document) {
    return getProbabilityDistribution(document).maxIndex();
  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    return getProbabilityDistribution(features);
  }

  private double getProbabilityForClass(DoubleVector document, int classIndex) {
    double probabilitySum = 0.0d;
    Iterator<DoubleVectorElement> iterateNonZero = document.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      double wordCount = next.getValue();
      double probabilityOfToken = probabilityMatrix.get(classIndex,
          next.getIndex());
      probabilitySum += (wordCount * probabilityOfToken);
    }
    return probabilitySum;
  }

  private DenseDoubleVector getProbabilityDistribution(DoubleVector document) {

    int numClasses = classProbability.getLength();
    DenseDoubleVector distribution = new DenseDoubleVector(numClasses);
    // loop through all classes and get the max probable one
    for (int i = 0; i < numClasses; i++) {
      double probability = getProbabilityForClass(document, i);
      distribution.set(i, probability);
    }

    double maxProbability = distribution.max();
    double probabilitySum = 0.0d;
    // we normalize it back
    for (int i = 0; i < numClasses; i++) {
      double probability = distribution.get(i);
      double normalizedProbability = Math.exp(probability - maxProbability)
          * classProbability.get(i);
      distribution.set(i, normalizedProbability);
      probabilitySum += normalizedProbability;
    }

    // since the sum is sometimes not 1, we need to divide by the sum
    distribution = (DenseDoubleVector) distribution.divide(probabilitySum);

    return distribution;
  }

  /**
   * @return the internal prior class probability.
   */
  public DenseDoubleVector getClassProbability() {
    return this.classProbability;
  }

  /**
   * @return the internal probability matrix.
   */
  public DenseDoubleMatrix getProbabilityMatrix() {
    return this.probabilityMatrix;
  }

}
