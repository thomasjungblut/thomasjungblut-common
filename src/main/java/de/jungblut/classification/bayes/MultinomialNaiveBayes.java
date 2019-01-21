package de.jungblut.classification.bayes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.datastructure.Iterables;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.writable.MatrixWritable;
import de.jungblut.writable.VectorWritable;

/**
 * Multinomial naive bayes classifier. This class now contains a sparse internal
 * representations of the "feature given class" probabilities. So this can be
 * scaled to very large text corpora and large numbers of classes easily.
 * Serialization and deserialization happens through the like-named static
 * methods.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MultinomialNaiveBayes extends AbstractClassifier {

  private static final double LOW_PROBABILITY = FastMath.log(1e-8);

  private DoubleMatrix probabilityMatrix;
  private DoubleVector classPriorProbability;

  private boolean verbose;

  /**
   * Default constructor to construct this classifier.
   */
  public MultinomialNaiveBayes() {
  }

  /**
   * Pass true if this classifier should output some progress information to
   * STDOUT.
   */
  public MultinomialNaiveBayes(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Deserialization constructor to instantiate an already trained classifier
   * from the internal representations.
   * 
   * @param probabilityMatrix the probability matrix.
   * @param classProbability the prior class probabilities.
   */
  private MultinomialNaiveBayes(DoubleMatrix probabilityMatrix,
      DoubleVector classProbability) {
    super();
    this.probabilityMatrix = probabilityMatrix;
    this.classPriorProbability = classProbability;
  }

  @Override
  public void train(Iterable<DoubleVector> features,
      Iterable<DoubleVector> outcome) {

    Iterator<DoubleVector> featureIterator = features.iterator();
    Iterator<DoubleVector> outcomeIterator = outcome.iterator();
    Tuple<DoubleVector, DoubleVector> first = Iterables.consumeNext(
        featureIterator, outcomeIterator);

    int numDistinctClasses = first.getSecond().getDimension();
    // respect the binary case
    numDistinctClasses = numDistinctClasses == 1 ? 2 : numDistinctClasses;
    // sparse row representations, so every class has the features as a hashset
    // of values. This gives good compression for many class problems.
    probabilityMatrix = new SparseDoubleRowMatrix(numDistinctClasses, first
        .getFirst().getDimension());

    int[] tokenPerClass = new int[numDistinctClasses];
    int[] numDocumentsPerClass = new int[numDistinctClasses];

    // init the probability with the document length = word count for each token
    // observe our first example, then loop until we have observed everything
    observe(first.getFirst(), first.getSecond(), numDistinctClasses,
        tokenPerClass, numDocumentsPerClass);
    int numDocumentsSeen = 1;
    while ((first = Iterables.consumeNext(featureIterator, outcomeIterator)) != null) {
      observe(first.getFirst(), first.getSecond(), numDistinctClasses,
          tokenPerClass, numDocumentsPerClass);
      numDocumentsSeen++;
    }

    // know we know the token distribution per class, we can calculate the
    // probability. It is intended for them to be negative in some cases
    for (int row = 0; row < numDistinctClasses; row++) {
      // we can quite efficiently iterate over the non-zero row vectors now
      DoubleVector rowVector = probabilityMatrix.getRowVector(row);
      // don't care about not occuring words, we honor them with a very small
      // probability later on when predicting, here we save a lot space.
      Iterator<DoubleVectorElement> iterateNonZero = rowVector.iterateNonZero();
      double normalizer = FastMath.log(tokenPerClass[row]
          + probabilityMatrix.getColumnCount() - 1);
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        double currentWordCount = next.getValue();
        double logProbability = FastMath.log(currentWordCount) - normalizer;
        probabilityMatrix.set(row, next.getIndex(), logProbability);
      }
      if (verbose) {
        System.out
            .println("Computed " + row + " / " + numDistinctClasses + "!");
      }
    }

    classPriorProbability = new DenseDoubleVector(numDistinctClasses);
    for (int i = 0; i < numDistinctClasses; i++) {
      double prior = FastMath.log(numDocumentsPerClass[i])
          - FastMath.log(numDocumentsSeen);
      classPriorProbability.set(i, prior);
    }
  }

  private void observe(DoubleVector document, DoubleVector outcome,
      int numDistinctClasses, int[] tokenPerClass, int[] numDocumentsPerClass) {
    int predictedClass = outcome.maxIndex();
    if (numDistinctClasses == 2) {
      predictedClass = (int) outcome.get(0);
    }
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
      if (probabilityOfToken == 0d) {
        probabilityOfToken = LOW_PROBABILITY;
      }
      probabilitySum += (wordCount * probabilityOfToken);
    }
    return probabilitySum;
  }

  private DenseDoubleVector getProbabilityDistribution(DoubleVector document) {

    int numClasses = classPriorProbability.getLength();
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
      double normalizedProbability = FastMath.exp(probability - maxProbability
          + classPriorProbability.get(i));
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
  DoubleVector getClassProbability() {
    return this.classPriorProbability;
  }

  /**
   * @return the internal probability matrix.
   */
  DoubleMatrix getProbabilityMatrix() {
    return this.probabilityMatrix;
  }

  /**
   * Deserializes a new MultinomialNaiveBayesClassifier from the given input
   * stream. Note that "in" will not be closed by this method.
   */
  public static MultinomialNaiveBayes deserialize(DataInput in)
      throws IOException {
    MatrixWritable matrixWritable = new MatrixWritable();
    matrixWritable.readFields(in);
    DoubleVector classProbability = VectorWritable.readVector(in);

    return new MultinomialNaiveBayes(matrixWritable.getMatrix(),
        classProbability);
  }

  public static void serialize(MultinomialNaiveBayes model, DataOutput out)
      throws IOException {
    new MatrixWritable(model.probabilityMatrix).write(out);
    VectorWritable.writeVector(model.classPriorProbability, out);
  }

}
