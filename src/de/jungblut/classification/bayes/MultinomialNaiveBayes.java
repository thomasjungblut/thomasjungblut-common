package de.jungblut.classification.bayes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Preconditions;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
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
  private DoubleVector classProbability;

  /**
   * Default constructor to construct this classifier.
   */
  public MultinomialNaiveBayes() {
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
    this.classProbability = classProbability;
  }

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    Preconditions.checkArgument(features.length > 0,
        "Features must contain at least a single item!");
    Preconditions.checkArgument(features.length == outcome.length,
        "There must be an equal amount of features and prediction outcomes!");
    int numDistinctClasses = outcome[0].getDimension();
    // respect the binary case
    numDistinctClasses = numDistinctClasses == 1 ? 2 : numDistinctClasses;
    // sparse row representations, so every class has the features as a hashset
    // of values. This gives good compression for many class problems.
    probabilityMatrix = new SparseDoubleRowMatrix(numDistinctClasses,
        features[0].getDimension());

    int[] tokenPerClass = new int[numDistinctClasses];
    int[] numDocumentsPerClass = new int[numDistinctClasses];

    // init the probability with the document length = word count for each token
    for (int columnIndex = 0; columnIndex < features.length; columnIndex++) {
      final DoubleVector document = features[columnIndex];
      int predictedClass = outcome[columnIndex].maxIndex();
      if (numDistinctClasses == 2) {
        predictedClass = (int) outcome[columnIndex].get(0);
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

    // know we know the token distribution per class, we can calculate the
    // probability. It is intended for them to be negative in some cases
    for (int row = 0; row < numDistinctClasses; row++) {
      for (int tokenColumn = 0; tokenColumn < probabilityMatrix
          .getColumnCount(); tokenColumn++) {
        double currentWordCount = probabilityMatrix.get(row, tokenColumn);
        // don't care about not occuring words, we honor them with a very small
        // probability later on.
        if (currentWordCount != 0) {
          double logProbability = FastMath.log(currentWordCount)
              - FastMath.log(tokenPerClass[row]
                  + probabilityMatrix.getColumnCount() - 1);
          probabilityMatrix.set(row, tokenColumn, logProbability);
        }
      }
    }

    classProbability = new DenseDoubleVector(numDistinctClasses);
    for (int i = 0; i < numDistinctClasses; i++) {
      double prior = FastMath.log(numDocumentsPerClass[i])
          - FastMath.log(features.length);
      classProbability.set(i, prior);
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
      if (probabilityOfToken == 0d) {
        probabilityOfToken = LOW_PROBABILITY;
      }
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
      double normalizedProbability = FastMath.exp(probability - maxProbability
          + classProbability.get(i));
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
    return this.classProbability;
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
    VectorWritable.writeVector(model.classProbability, out);
  }

}
