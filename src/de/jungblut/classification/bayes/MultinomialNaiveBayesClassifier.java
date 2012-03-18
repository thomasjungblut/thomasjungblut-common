package de.jungblut.classification.bayes;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.util.Tuple;
import de.jungblut.util.Tuple3;

/**
 * Simple multinomial naive bayes classifier.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MultinomialNaiveBayesClassifier {

  private DenseDoubleMatrix probabilityMatrix;
  private DenseDoubleVector classProbability;

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
      probabilityMatrix.set(predictedClass, columnIndex, document.getLength());
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
    return new Tuple<DenseDoubleMatrix, DenseDoubleVector>(probabilityMatrix,
        classProbability);
  }

  /**
   * Returns the maximum likely class.
   */
  public final int classify(DoubleVector document) {
    return getProbabilityDistribution(document).maxIndex();
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

  public static void main(String[] args) {

    Tuple3<List<String[]>, DenseIntVector, String[]> readTwentyNewsgroups = TwentyNewsgroupReader
        .readTwentyNewsgroups(new File(
            "files/20news-bydate/20news-bydate-train/"));
    
    readTwentyNewsgroups.getFirst();
    
    

  }

}
