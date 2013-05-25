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

import de.jungblut.classification.Evaluator;
import de.jungblut.classification.Evaluator.EvaluationResult;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
import de.jungblut.math.tuple.Tuple;

public class NamedEntityRecognitionTest extends TestCase {

  @Test
  public void testEndToEnd() throws Exception {

    String train = "files/ner/train";
    String test = "files/ner/dev";

    List<String> lines = Files.readAllLines(
        FileSystems.getDefault().getPath(train), Charset.defaultCharset())
        .subList(0, 2000);
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

    SparseFeatureExtractorHelper<String> fact = new SparseFeatureExtractorHelper<>(
        words, labels, new BasicFeatureExtractor());
    Tuple<DoubleVector[], DoubleVector[]> vectorize = fact.vectorize();
    DoubleVector[] features = vectorize.getFirst();
    DoubleVector[] outcome = vectorize.getSecond();

    Tuple<DoubleVector[], DoubleVector[]> vectorizeAdditionals = fact
        .vectorizeAdditionals(testWords, testLabels);
    DoubleVector[] testFeatures = vectorizeAdditionals.getFirst();
    DoubleVector[] testOutcome = vectorizeAdditionals.getSecond();

    MaxEntMarkovModel model = new MaxEntMarkovModel(new Fmincg(), 100, false);
    DoubleVector[] vectorizeEachLabel = fact.vectorizeEachLabel(testWords);
    UnrollableDoubleVector[] unrollableTestFeatures = new UnrollableDoubleVector[testFeatures.length];
    for (int i = 0; i < testFeatures.length; i++) {
      if (i == 0) {
        unrollableTestFeatures[i] = new UnrollableDoubleVector(testFeatures[i],
            new DoubleVector[] { vectorizeEachLabel[i] });
      } else {
        unrollableTestFeatures[i] = new UnrollableDoubleVector(testFeatures[i],
            new DoubleVector[] { vectorizeEachLabel[i],
                vectorizeEachLabel[i + 1] });
      }
    }

    EvaluationResult res = Evaluator.evaluateSplit(model, 2, 0.5d, features,
        outcome, unrollableTestFeatures, testOutcome);

    assertTrue(res.getPrecision() > 0.95);
    assertTrue(res.getAccuracy() > 0.95);

    DoubleMatrix predict = model.predict(
        new SparseDoubleRowMatrix(testFeatures), new SparseDoubleRowMatrix(
            vectorizeEachLabel));
    // check if specific names are included
    HashSet<String> names = new HashSet<>(Arrays.asList("Benjamin",
        "Netanyahu", "David", "Levy", "Jones"));

    for (int i = 0; i < predict.getRowCount(); i++) {
      int predictedClass = (int) predict.get(i, 0);
      if (predictedClass == 1) {
        names.remove((testWords.get(i)));
      }
    }

    assertEquals(0, names.size());

  }
}
