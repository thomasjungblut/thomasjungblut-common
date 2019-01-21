package de.jungblut.clustering;

import de.jungblut.distance.EuclidianDistance;
import de.jungblut.jrpt.KDTree;
import de.jungblut.jrpt.VectorDistanceTuple;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.random.Well1024a;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MeanShiftClusteringTest {

    @Test
    public void testKDLookup() {
        HashSet<DoubleVector> lefts = new HashSet<>();
        List<DoubleVector> points = drawTwoDistinctDistributions(lefts,
                System.currentTimeMillis());
        KDTree<Integer> kdTree = new KDTree<>();
        Stream<Tuple<DoubleVector, Integer>> payloadStream = IntStream.range(0,
                points.size()).mapToObj(i -> new Tuple<>(points.get(i), i));

        kdTree.constructWithPayload(payloadStream);

        double maxRadius = new EuclidianDistance().measureDistance(
                new double[]{250}, new double[]{351});

        List<VectorDistanceTuple<Integer>> neighbours = kdTree
                .getNearestNeighbours(new DenseDoubleVector(new double[]{250}),
                        maxRadius);
        for (de.jungblut.jrpt.VectorDistanceTuple<Integer> x : neighbours) {
            lefts.remove(x.getVector());
        }
        assertEquals(0, lefts.size());
    }

    @Test
    public void testMeanShiftClustering() {
        double h = 10;
        List<DoubleVector> centers = MeanShiftClustering.cluster(
                drawTwoDistinctDistributions(null, 5L), h, 50, 2000, false);
        assertEquals(2, centers.size());
        assertEquals(centers.get(0).get(0), 244, 5);
        assertEquals(centers.get(1).get(0), 742, 5);
    }

    public List<DoubleVector> drawTwoDistinctDistributions(
            HashSet<DoubleVector> leftDistribution, long seed) {
        List<DoubleVector> lst = new ArrayList<>(100);

        double mean1 = 250;
        double mean2 = 750;
        RandomDataImpl random = new RandomDataImpl(new Well1024a(seed));
        for (int i = 0; i < 50; i++) {
            double nextGaussian1 = random.nextGaussian(mean1, Math.sqrt(100));
            assertTrue(nextGaussian1 >= 150 && nextGaussian1 <= 350);
            double nextGaussian2 = random.nextGaussian(mean2, Math.sqrt(100));
            assertTrue(nextGaussian2 >= 650 && nextGaussian2 <= 850);
            DenseDoubleVector lef = new DenseDoubleVector(
                    new double[]{nextGaussian1});
            lst.add(lef);
            if (leftDistribution != null) {
                leftDistribution.add(lef);
            }
            lst.add(new DenseDoubleVector(new double[]{nextGaussian2}));
        }

        return lst;
    }
}
