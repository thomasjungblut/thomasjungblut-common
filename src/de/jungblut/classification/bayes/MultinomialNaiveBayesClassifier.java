package de.jungblut.classification.bayes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.math.tuple.Tuple;

/**
 * Simple multinomial naive bayes classifier.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MultinomialNaiveBayesClassifier extends AbstractClassifier {

  private DenseDoubleMatrix probabilityMatrix;
  private DenseDoubleVector classProbability;

  @Override
  public void train(DoubleVector[] features, DenseDoubleVector[] outcome) {
    int[] classes = new int[outcome.length];
    for (int i = 0; i < outcome.length; i++) {
      classes[i] = outcome[i].maxIndex();
    }
    train(new SparseDoubleColumnMatrix(Arrays.asList(features)),
        new DenseIntVector(classes));
  }

  public final Tuple<DenseDoubleMatrix, DenseDoubleVector> train(
      SparseDoubleColumnMatrix documentWordCounts, DenseIntVector prediction) {

    final int numDistinctElements = prediction
        .getNumberOfDistinctElementsFast();
    probabilityMatrix = new DenseDoubleMatrix(numDistinctElements,
        documentWordCounts.getRowCount(), 1.0d);

    int[] columnIndices = documentWordCounts.columnIndices();
    int[] tokenPerClass = new int[numDistinctElements];
    int[] numDocumentsPerClass = new int[numDistinctElements];

    // init the probability with the document length = word count for each token
    for (int columnIndex : columnIndices) {
      final DoubleVector document = documentWordCounts
          .getColumnVector(columnIndex);
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
    for (int row = 0; row < numDistinctElements; row++) {
      for (int col = 0; col < probabilityMatrix.getColumnCount(); col++) {
        double currentWordCount = probabilityMatrix.get(row, col);
        double logLikelyhood = Math.log(currentWordCount
            / (tokenPerClass[row] + probabilityMatrix.getColumnCount() - 1));
        probabilityMatrix.set(row, col, logLikelyhood);
      }
    }

    classProbability = new DenseDoubleVector(numDistinctElements);
    for (int i = 0; i < numDistinctElements; i++) {
      classProbability.set(i, (numDocumentsPerClass[i] + 1)
          / (double) columnIndices.length);
    }
    return new Tuple<>(probabilityMatrix, classProbability);
  }

  /**
   * Returns the maximum likely class.
   */
  public final int classify(DoubleVector document) {
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

  public final DenseDoubleVector getProbabilityDistribution(
      DoubleVector document) {

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
   * This method prints a confusion matrix along with several metrics like
   * accuracy. It prints to STDOUT if verbose.
   * 
   * @return returns accuracy.
   */
  public double evaluateModel(List<DoubleVector> testSetInputVector,
      DenseIntVector testSetPrediction, String[] classNames, boolean verbose) {

    // we add two columns to the confusion matrix to add false sums and overall
    // sums to the rows
    final int columnLength = classProbability.getLength() + 2;
    int[][] confusionMatrix = new int[classProbability.getLength()][columnLength];
    int truePositives = 0;
    int index = 0;
    for (DoubleVector v : testSetInputVector) {
      int classifiedClass = this.classify(v);
      int realClass = testSetPrediction.get(index);
      if (classifiedClass == realClass) {
        truePositives++;
      } else {
        confusionMatrix[classifiedClass][classProbability.getLength() + 1]++;
      }

      confusionMatrix[classifiedClass][realClass]++;
      confusionMatrix[classifiedClass][classProbability.getLength()]++;

      index++;
    }

    double accuracy = truePositives / (double) testSetInputVector.size();
    if (verbose) {
      System.out.println("Classified correctly: " + truePositives + " out of "
          + testSetInputVector.size() + " documents! That's accuracy of "
          + (accuracy * 100) + "%");
    }

    if (verbose) {
      System.out.println("\nConfusion matrix:\n");

      for (int i = 0; i < classProbability.getLength(); i++) {
        System.out.format("%5d", i);
      }

      System.out.println("  SUM  FALSE\n");

      for (int i = 0; i < classProbability.getLength(); i++) {
        for (int j = 0; j < columnLength; j++) {
          System.out.format("%5d", confusionMatrix[i][j]);
        }
        String clz = classNames != null ? classNames[i] : i + "";
        System.out.println(" <- " + i + " classfied as " + clz);
      }
    }
    return accuracy;
  }

}
