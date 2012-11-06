package de.jungblut.classification.bayes;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.collect.HashMultiset;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.math.tuple.Tuple;
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
    Tuple<HashMultiset<String>[], String[]> trainingSetWordCounts = VectorizerUtils
        .prepareWordCountToken(trainingDocuments);
    List<DoubleVector> trainingSetInputVector = VectorizerUtils
        .wordFrequencyVectorize(trainingDocuments, trainingSetWordCounts);

    MultinomialNaiveBayesClassifier classifier = new MultinomialNaiveBayesClassifier();
    classifier.train(new SparseDoubleColumnMatrix(trainingSetInputVector),
        trainingSet.getSecond());

    Tuple3<List<String[]>, DenseIntVector, String[]> testSet = TwentyNewsgroupReader
        .readTwentyNewsgroups(new File(
            "files/20news-bydate/20news-bydate-test/"));

    List<String[]> testDocuments = testSet.getFirst();
    Tuple<HashMultiset<String>[], String[]> updatedWordFrequency = VectorizerUtils
        .updateWordFrequencyCounts(testDocuments,
            trainingSetWordCounts.getSecond());
    List<DoubleVector> testSetInputVector = VectorizerUtils
        .wordFrequencyVectorize(testDocuments, updatedWordFrequency);
    DenseIntVector testSetPrediction = testSet.getSecond();
    double evaluateModel = classifier.evaluateModel(testSetInputVector,
        testSetPrediction, trainingSet.getThird(), false);

    assertTrue(evaluateModel > 0.6d);

  }

}
