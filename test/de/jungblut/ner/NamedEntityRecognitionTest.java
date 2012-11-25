package de.jungblut.ner;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.classification.Evaluator;
import de.jungblut.classification.Evaluator.EvaluationResult;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
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

    List<String> testwords = new ArrayList<>(lines.size());
    List<Integer> testlabels = new ArrayList<>(lines.size());

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
        testwords.add(split[0]);
        testlabels.add(split[1].equals("O") ? 0 : 1);
      }
    }

    SparseFeatureExtractorHelper fact = new SparseFeatureExtractorHelper(words,
        labels, new BasicFeatureExtractor());
    Tuple<DoubleVector[], DenseDoubleVector[]> vectorize = fact.vectorize();
    DoubleVector[] features = vectorize.getFirst();
    DenseDoubleVector[] outcome = vectorize.getSecond();

    Tuple<DoubleVector[], DenseDoubleVector[]> vectorizeAdditionals = fact
        .vectorizeAdditionals(testwords, testlabels);
    DoubleVector[] testFeatures = vectorizeAdditionals.getFirst();
    DenseDoubleVector[] testOutcome = vectorizeAdditionals.getSecond();

    MaxEntMarkovModel model = new MaxEntMarkovModel(new Fmincg(), 250, false);
    EvaluationResult res = Evaluator.evaluateSplit(model, 2, 0.5d, features,
        outcome, testFeatures, testOutcome);
    assertTrue(res.getPrecision() > 0.9);
    assertTrue(res.getAccuracy() > 0.9);
    assertTrue(res.getCorrect() > 50500);
  }

}
