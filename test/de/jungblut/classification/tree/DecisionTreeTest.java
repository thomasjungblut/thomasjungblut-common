package de.jungblut.classification.tree;

import static org.junit.Assert.assertEquals;
import gnu.trove.set.hash.TIntHashSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Test;

import de.jungblut.classification.eval.EvaluationSplit;
import de.jungblut.classification.eval.Evaluator;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.IrisReader;
import de.jungblut.reader.MushroomReader;

public class DecisionTreeTest {

  static Dataset mushroom = MushroomReader
      .readMushroomDataset("files/mushroom/mushroom_dataset.csv");
  static EvaluationSplit irisEvaluationSplit = IrisReader
      .getEvaluationSplit(IrisReader
          .readIrisDataset("files/iris/iris_dataset.csv"));

  @Test
  public void testMushroomNominalData() {
    DecisionTree tree = DecisionTree.create();
    // nominal features are default here anyway
    EvaluationResult res = Evaluator.evaluateClassifier(tree,
        mushroom.getFeatures(), mushroom.getOutcomes(), 2, 0.9f, false);
    assertEquals(1d, res.getAccuracy(), 1e-5);
  }

  @Test
  public void testIrisNumericalData() {
    DecisionTree tree = DecisionTree.create(FeatureType.allNumerical(4));
    EvaluationResult res = Evaluator.evaluateSplit(tree, 3, null,
        irisEvaluationSplit);
    assertEquals(27, res.getCorrect()); // 27 out of 30 is really good!
    assertEquals(0.9, res.getAccuracy(), 1e-5);
  }

  @Test
  public void testCompiledMushroomNominalData() {
    DecisionTree tree = DecisionTree.createCompiledTree();
    // nominal features are default here anyway
    EvaluationResult res = Evaluator.evaluateClassifier(tree,
        mushroom.getFeatures(), mushroom.getOutcomes(), 2, 0.9f, false);
    assertEquals(1d, res.getAccuracy(), 1e-5);
  }

  @Test
  public void testCompiledIrisNumericalData() {
    DecisionTree tree = DecisionTree.createCompiledTree(FeatureType
        .allNumerical(4));
    EvaluationResult res = Evaluator.evaluateSplit(tree, 3, null,
        irisEvaluationSplit);
    assertEquals(27, res.getCorrect());
    assertEquals(0.9, res.getAccuracy(), 1e-5);
  }

  @Test
  public void testSerialization() throws Exception {
    DecisionTree tree = DecisionTree.create();
    // train
    tree.train(mushroom.getFeatures(), mushroom.getOutcomes());
    // uncompiled
    EvaluationResult res = Evaluator.testClassifier(tree, 2, null,
        mushroom.getFeatures().length, mushroom.getFeatures(),
        mushroom.getOutcomes());
    assertEquals(1d, res.getAccuracy(), 1e-5);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DecisionTree.serialize(tree, new DataOutputStream(baos));
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DecisionTree deserialized = DecisionTree.deserialize(new DataInputStream(
        bais));

    // now let's eval on the train set, it should classify everything correct as
    // it overfits the data.
    res = Evaluator.testClassifier(deserialized, 2, null,
        mushroom.getFeatures().length, mushroom.getFeatures(),
        mushroom.getOutcomes());
    assertEquals(1d, res.getAccuracy(), 1e-5);

  }

  @Test
  public void testCompiledSerialization() throws Exception {
    DecisionTree tree = DecisionTree.create();
    tree.setCompiled(true);
    // train
    tree.train(mushroom.getFeatures(), mushroom.getOutcomes());
    // uncompiled
    EvaluationResult res = Evaluator.testClassifier(tree, 2, null,
        mushroom.getFeatures().length, mushroom.getFeatures(),
        mushroom.getOutcomes());
    assertEquals(1d, res.getAccuracy(), 1e-5);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DecisionTree.serialize(tree, new DataOutputStream(baos));
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DecisionTree deserialized = DecisionTree.deserialize(new DataInputStream(
        bais));

    // now let's eval on the train set, it should classify everything correct as
    // it overfits the data.
    res = Evaluator.testClassifier(deserialized, 2, null,
        mushroom.getFeatures().length, mushroom.getFeatures(),
        mushroom.getOutcomes());
    assertEquals(1d, res.getAccuracy(), 1e-5);
  }

  @Test
  public void testPossibleFeatures() {
    DecisionTree tree = DecisionTree.create();
    tree.setNumFeatures(10);

    TIntHashSet set = tree.getPossibleFeatures();
    assertEquals(10, set.size());
    for (int i = 0; i < 10; i++) {
      set.remove(i);
    }
    assertEquals(0, set.size());

    tree.setNumRandomFeaturesToChoose(3);
    tree.setNumFeatures(10);
    set = tree.chooseRandomFeatures(tree.getPossibleFeatures());
    assertEquals(3, set.size());

    tree.setNumRandomFeaturesToChoose(3);
    tree.setNumFeatures(2);
    set = tree.chooseRandomFeatures(tree.getPossibleFeatures());
    assertEquals(2, set.size());
  }

  @Test
  public void testEntropy() {
    int[] outcomeCounter = new int[] { 5, 9 };
    double entropy = DecisionTree.getEntropy(outcomeCounter);
    assertEquals(0.94, entropy, 1e-3);
  }

}
