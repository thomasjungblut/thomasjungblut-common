package de.jungblut.classification.tree;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TreeCompilerTest {

    private AbstractTreeNode compiledRoot;

    @Before
    public void setup() throws Exception {
        NominalNode nd = new NominalNode(2, 3);
        nd.nominalSplitValues[0] = 5;
        nd.children[0] = new LeafNode(25);
        nd.nominalSplitValues[1] = 8;
        nd.children[1] = new LeafNode(88);
        nd.nominalSplitValues[2] = 18;
        nd.children[2] = new LeafNode(33);

        AbstractTreeNode root = new NumericalNode(0, 1, nd, new NumericalNode(1, 5,
                new LeafNode(1337), new LeafNode(26)));
        compiledRoot = TreeCompiler.compileAndLoad(
                TreeCompiler.generateClassName(), root);
    }

    @Test
    public void testNominalSwitches() {
        DoubleVector vec = new DenseDoubleVector(new double[]{0, 0, 0});
        int result = compiledRoot.predict(vec);
        Assert.assertEquals(0, result);

        vec = new DenseDoubleVector(new double[]{1, 0, 5});
        result = compiledRoot.predict(vec);
        Assert.assertEquals(25, result);

        vec = new DenseDoubleVector(new double[]{-1, 0, 8});
        result = compiledRoot.predict(vec);
        Assert.assertEquals(88, result);

        vec = new DenseDoubleVector(new double[]{0, 0, 18});
        result = compiledRoot.predict(vec);
        Assert.assertEquals(33, result);
    }

    @Test
    public void testOtherResults() {

        DoubleVector vec = new DenseDoubleVector(new double[]{2, 2, 0});
        int result = compiledRoot.predict(vec);
        Assert.assertEquals(1337, result);

        vec = new DenseDoubleVector(new double[]{2, 18, 0});
        result = compiledRoot.predict(vec);
        Assert.assertEquals(26, result);

    }

}
