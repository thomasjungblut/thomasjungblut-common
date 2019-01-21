package de.jungblut.clustering;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import de.jungblut.clustering.AgglomerativeClustering.ClusterNode;
import de.jungblut.distance.ManhattanDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class AgglomerativeClusteringTest {

    @Test
    public void testClustering() {
        ArrayList<DoubleVector> vecs = new ArrayList<>();
        vecs.add(new DenseDoubleVector(new double[]{0, 5}));
        vecs.add(new DenseDoubleVector(new double[]{0, 6}));
        vecs.add(new DenseDoubleVector(new double[]{6, 5}));
        vecs.add(new DenseDoubleVector(new double[]{6, 6}));
        vecs.add(new DenseDoubleVector(new double[]{10, 10}));
        vecs.add(new DenseDoubleVector(new double[]{5, 0}));

        HashMultimap<Integer, double[]> result = HashMultimap.create();
        result.put(0, new double[]{5.25, 5.25});
        result.put(1, new double[]{3.0, 5.5});
        result.put(1, new double[]{7.5, 5.0});
        result.put(2, new double[]{0.0, 5.5});
        result.put(2, new double[]{10.0, 10.0});
        result.put(2, new double[]{5.0, 0.0});
        result.put(2, new double[]{5.0, 0.0});
        result.put(3, new double[]{0.0, 5.0});
        result.put(3, new double[]{0.0, 6.0});
        result.put(3, new double[]{6.0, 5.0});
        result.put(3, new double[]{6.0, 6.0});

        List<List<ClusterNode>> clusters = AgglomerativeClustering.cluster(vecs,
                new ManhattanDistance(), true);
        assertEquals(4, clusters.size());
        assertEquals(1, clusters.get(0).size());
        assertEquals(2, clusters.get(1).size());
        assertEquals(3, clusters.get(2).size());
        assertEquals(6, clusters.get(3).size());

        ClusterNode clusterNode = clusters.get(0).get(0);
        traverse(clusterNode, 0, result);

        // check if all our points were in the right cluster levels
        assertEquals(0, result.size());
    }

    public void traverse(ClusterNode clusterNode, int level,
                         HashMultimap<Integer, double[]> result) {
        System.out.println(level + " " + Strings.repeat("\t", level)
                + clusterNode.getMean());
        double[] array = clusterNode.getMean().toArray();
        Set<double[]> set = result.get(level);
        Iterator<double[]> iterator = set.iterator();
        while (iterator.hasNext()) {
            if (Arrays.equals(iterator.next(), array))
                iterator.remove();
        }
        if (clusterNode.getLeft() != null) {
            traverse(clusterNode.getLeft(), level + 1, result);
        }
        if (clusterNode.getRight() != null) {
            traverse(clusterNode.getRight(), level + 1, result);
        }
    }
}
