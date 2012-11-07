package de.jungblut.classification.bayes;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.math.tuple.Tuple3;
import de.jungblut.nlp.VectorizerUtils;
import de.jungblut.reader.TwentyNewsgroupReader;

public class MultinomialNaiveBayesTest extends TestCase {

  @Test
  public void testNaiveBayes() {
    // TODO this is actually overkill and not a good testcase at all..
    Tuple3<List<String[]>, DenseIntVector, String[]> trainingSet = TwentyNewsgroupReader
        .readTwentyNewsgroups(new File(
            "files/20news-bydate/20news-bydate-train/"));

    List<String[]> trainingDocuments = trainingSet.getFirst();
    String[] dict = VectorizerUtils.buildDictionary(trainingDocuments);
    List<DoubleVector> trainingSetInputVector = VectorizerUtils.tfIdfVectorize(
        trainingDocuments, dict, VectorizerUtils
            .buildInvertedIndexDocumentCount(trainingDocuments, dict));

    MultinomialNaiveBayesClassifier classifier = new MultinomialNaiveBayesClassifier();
    classifier.train(new SparseDoubleColumnMatrix(trainingSetInputVector),
        trainingSet.getSecond());

    Tuple3<List<String[]>, DenseIntVector, String[]> testSet = TwentyNewsgroupReader
        .readTwentyNewsgroups(new File(
            "files/20news-bydate/20news-bydate-test/"));

    List<String[]> testDocuments = testSet.getFirst();
    List<DoubleVector> testSetInputVector = VectorizerUtils.tfIdfVectorize(
        testDocuments, dict,
        VectorizerUtils.buildInvertedIndexDocumentCount(testDocuments, dict));
    DenseIntVector testSetPrediction = testSet.getSecond();
    double evaluateModel = classifier.evaluateModel(testSetInputVector,
        testSetPrediction, trainingSet.getThird(), false);

    System.out.println(evaluateModel);
    assertTrue(evaluateModel > 0.7d);

  }

}
