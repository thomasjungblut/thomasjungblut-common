package de.jungblut.classification.tree;

import gnu.trove.set.hash.TIntHashSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecisionTreeTest {

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
        int[] outcomeCounter = new int[]{5, 9};
        double entropy = DecisionTree.getEntropy(outcomeCounter);
        assertEquals(0.94, entropy, 1e-3);
    }

}
