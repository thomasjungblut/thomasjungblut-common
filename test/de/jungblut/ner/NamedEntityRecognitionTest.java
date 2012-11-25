package de.jungblut.ner;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
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

    String train = "files/ner/train";

    List<String> lines = Files.readAllLines(
        FileSystems.getDefault().getPath(train), Charset.defaultCharset());

    List<String> words = new ArrayList<>(lines.size());
    List<Integer> labels = new ArrayList<>(lines.size());

    for (String line : lines) {
      String[] split = line.trim().split("\\s+");
      if (!line.isEmpty() && split.length == 2) {
        words.add(split[0]);
        labels.add(split[1].equals("O") ? 0 : 1);
      }
    }

    SparseFeatureExtractorHelper fact = new SparseFeatureExtractorHelper(words, labels,
        new BasicFeatureExtractor());
    Tuple<DoubleVector[], DenseDoubleVector[]> vectorize = fact.vectorize();
    DoubleVector[] features = vectorize.getFirst();
    DenseDoubleVector[] outcome = vectorize.getSecond();

    MaxEntMarkovModel model = new MaxEntMarkovModel(new Fmincg(), 100, true);
    model.train(features, outcome);
    DoubleMatrix predict = model.predict(new SparseDoubleRowMatrix(features));
    double fails = 0d;
    for (int i = 0; i < features.length; i++) {
      fails += Math.abs(outcome[i].get(0) - predict.get(i, 0));
    }

    assertEquals(11128, (int) fails);
    System.out.println("Fails: " + fails + " / " + features.length
        + " = Error: " + (fails / features.length * 100) + "%");

  }

}
