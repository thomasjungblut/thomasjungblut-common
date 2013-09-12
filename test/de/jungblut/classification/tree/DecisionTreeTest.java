package de.jungblut.classification.tree;

import static org.junit.Assert.assertEquals;
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Test;

import de.jungblut.classification.eval.EvaluationSplit;
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
    DecisionTree tree = DecisionTree.create();
    // nominal features are default here anyway
    EvaluationResult res = Evaluator.evaluateClassifier(tree,
        mushroom.getFeatures(), mushroom.getOutcomes(), 2, 0.9f, false);
    assertEquals(1d, res.getAccuracy(), 1e-5);
  }

  @Test
  public void testIrisNumericalData() {
    Dataset iris = IrisReader.readIrisDataset("files/iris/iris_dataset.csv");
    EvaluationSplit evaluationSplit = IrisReader.getEvaluationSplit(iris);
    DecisionTree tree = DecisionTree.create(FeatureType.allNumerical(4));
    EvaluationResult res = Evaluator.evaluateSplit(tree, 3, null,
        evaluationSplit);
    assertEquals(27, res.getCorrect()); // 27 out of 30 is really good!
    assertEquals(0.9, res.getAccuracy(), 1e-5);
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
  }

  @Test
  public void testEntropy() {
    int[] outcomeCounter = new int[] { 5, 9 };
    double entropy = DecisionTree.getEntropy(outcomeCounter);
    assertEquals(0.94, entropy, 1e-3);
  }

}
