package de.jungblut.clustering;

import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KMeansClusteringTest {

    @Test
    public void testKMeansClustering() {
        ArrayList<DoubleVector> lst = getClusteringInput();

        KMeansClustering clusterer = new KMeansClustering(2, lst, false);
        EuclidianDistance dist = new EuclidianDistance();
        List<Cluster> assignments = clusterer.cluster(100, dist, 0.1d, false);
        DoubleVector[] centers = clusterer.getCenters();
        assertEquals(49.5, centers[0].get(0), 1e-4);
        assertEquals(24.5, centers[0].get(1), 1e-4);
        assertEquals(49.5, centers[1].get(0), 1e-4);
        assertEquals(74.5, centers[1].get(1), 1e-4);

        // the centers should partition the space in half
        assertEquals(2, assignments.size());
        assertEquals(5000, assignments.get(0).getAssignments().size());
        assertEquals(5000, assignments.get(1).getAssignments().size());
        // now verify the assignments
        for (DoubleVector v : assignments.get(0).getAssignments()) {
            double distRightCenter = dist.measureDistance(v, centers[0]);
            double distOtherCenter = dist.measureDistance(v, centers[1]);
            assertTrue(distRightCenter < distOtherCenter);
        }
        for (DoubleVector v : assignments.get(1).getAssignments()) {
            double distRightCenter = dist.measureDistance(v, centers[1]);
            double distOtherCenter = dist.measureDistance(v, centers[0]);
            assertTrue(distRightCenter < distOtherCenter);
        }
    }

    public static ArrayList<DoubleVector> getClusteringInput() {
        // we are "sampling" a 100x100 grid to a vector space and let's do some
        // clustering.
        return getClusteringInput(100, 100);
    }

    public static ArrayList<DoubleVector> getClusteringInput(int x, int y) {
        ArrayList<DoubleVector> lst = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                lst.add(new DenseDoubleVector(new double[]{i, j}));
            }
        }
        return lst;
    }

}
