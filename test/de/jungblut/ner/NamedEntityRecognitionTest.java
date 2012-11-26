package de.jungblut.ner;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
import de.jungblut.math.tuple.Tuple;

public class NamedEntityRecognitionTest extends TestCase {

  @Test
  public void testEndToEnd() throws Exception {

    String train = "E:/datasets/pa4-ner/data/train";
    String test = "E:/datasets/pa4-ner/data/dev";

    List<String> lines = Files.readAllLines(
        FileSystems.getDefault().getPath(train), Charset.defaultCharset());
    List<String> testLines = Files.readAllLines(FileSystems.getDefault()
        .getPath(test), Charset.defaultCharset());

    List<String> words = new ArrayList<>(lines.size());
    List<Integer> labels = new ArrayList<>(lines.size());

    List<String> testWords = new ArrayList<>(lines.size());
    List<Integer> testLabels = new ArrayList<>(lines.size());

    for (String line : lines) {
      String[] split = line.trim().split("\\s+");
      if (!line.isEmpty() && split.length == 2) {
        words.add(split[0]);
        labels.add(split[1].equals("O") ? 0 : 1);
      }
    }

    for (String line : testLines) {
      String[] split = line.trim().split("\\s+");
      if (!line.isEmpty() && split.length == 2) {
        testWords.add(split[0]);
        testLabels.add(split[1].equals("O") ? 0 : 1);
      }
    }

    SparseFeatureExtractorHelper fact = new SparseFeatureExtractorHelper(words,
        labels, new BasicFeatureExtractor());
    Tuple<DoubleVector[], DenseDoubleVector[]> vectorize = fact.vectorize();
    DoubleVector[] features = vectorize.getFirst();
    DenseDoubleVector[] outcome = vectorize.getSecond();

    Tuple<DoubleVector[], DenseDoubleVector[]> vectorizeAdditionals = fact
        .vectorizeAdditionals(testWords, testLabels);
    DoubleVector[] testFeatures = vectorizeAdditionals.getFirst();

    MaxEntMarkovModel model = new MaxEntMarkovModel(new Fmincg(), 100, false);
    model.train(features, outcome);
    DoubleVector[] vectorizeEachLabel = fact.vectorizeEachLabel(testWords);
    DoubleMatrix predict = model.predict(
        new SparseDoubleRowMatrix(testFeatures), new SparseDoubleRowMatrix(
            vectorizeEachLabel));
    // check if specific names are included
    HashSet<String> names = new HashSet<>(Arrays.asList("Clinton", "Carl",
        "Vinson", "Marc", "Dutroux", "Jens", "Howard"));

    for (int i = 0; i < predict.getRowCount(); i++) {
      int predictedClass = (int) predict.get(i, 0);
      if (predictedClass == 1) {
        names.remove((testWords.get(i)));
      }
    }

    assertEquals(0, names.size());

  }
}
