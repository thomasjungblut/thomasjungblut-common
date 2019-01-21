package de.jungblut.clustering;

import de.jungblut.distance.EuclidianDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DBSCANTest {

    @Test
    public void testDBSCAN() {
        ArrayList<DoubleVector> input = KMeansClusteringTest.getClusteringInput(10,
                10);
        // add some noise!
        input.add(new DenseDoubleVector(new double[]{2000, 2000}));

        EuclidianDistance measure = new EuclidianDistance();

        DBSCAN scan = new DBSCAN();
        ArrayList<DoubleVector>[] cluster = scan.cluster(input, measure, 5, 100);
        // we should just have a single connected cluster with all the points, but
        // the noise
        assertEquals(1, cluster.length);
        assertEquals(input.size() - 1, cluster[0].size());

        // check the noise
        List<DoubleVector> noise = scan.getNoise();
        assertEquals(1, noise.size());
        assertEquals(noise.get(0), input.get(input.size() - 1));
    }

}
