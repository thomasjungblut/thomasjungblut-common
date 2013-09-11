package de.jungblut.classification.tree;

import static org.junit.Assert.assertEquals;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;

import org.junit.Test;

import de.jungblut.classification.eval.Evaluator;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.IrisReader;
import de.jungblut.reader.MushroomReader;

public class DecisionTreeTest {

  @Test
  public void testMushroomNominalData() {
    Dataset mushroom = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");

    DecisionTree tree = new DecisionTree();
    EvaluationResult res = Evaluator.evaluateClassifier(tree,
        mushroom.getFeatures(), mushroom.getOutcomes(), 2, 0.9f, false);
    assertEquals(1d, res.getAccuracy(), 1e-5);
  }

  @Test
  public void testIrisNumericalData() {
    Dataset iris = IrisReader.readIrisDataset("files/iris/iris_dataset.csv");
    DecisionTree tree = new DecisionTree();
    FeatureType[] types = new FeatureType[4];
    Arrays.fill(types, FeatureType.NUMERICAL);
    tree.setFeatureTypes(types);
    EvaluationResult res = Evaluator.evaluateClassifier(tree,
        iris.getFeatures(), iris.getOutcomes(), 2, 0.9f, true);
    res.print();
    // TODO needs some assertions
  }

  @Test
  public void testPossibleFeatures() {
    DecisionTree tree = new DecisionTree();
    tree.setNumFeatures(10);

    TIntHashSet set = tree.getPossibleFeatures();
    assertEquals(10, set.size());
    for (int i = 0; i < 10; i++) {
      set.remove(i);
    }
    assertEquals(0, set.size());

    tree.setNumRandomFeaturesToChoose(3);
    tree.setNumFeatures(10);
    set = tree.getPossibleFeatures();
    assertEquals(3, set.size());
  }

  @Test
  public void testEntropy() {
    int[] outcomeCounter = new int[] { 5, 9 };
    double entropy = DecisionTree.getEntropy(outcomeCounter);
    assertEquals(0.94, entropy, 1e-3);
  }

}
