package de.jungblut.classification.knn;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.SingleEntryDoubleVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class KNearestNeighboursTest {

    @Test
    public void testMultiPrediction() {

        KNearestNeighbours knn = new KNearestNeighbours(5, 2);
        List<DoubleVector> features = new ArrayList<>();
        List<DoubleVector> outcome = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            features.add(new SingleEntryDoubleVector(i));
            double[] arr = new double[5];
            arr[i % 5] = 1d;
            outcome.add(new DenseDoubleVector(arr));
        }
        knn.train(features, outcome);

        DoubleVector prediction = knn.predict(new SingleEntryDoubleVector(5));
        assertArrayEquals(new double[]{1d, 0, 0, 0, 1d}, prediction.toArray());
        prediction = knn.predictProbability(new SingleEntryDoubleVector(5));
        assertArrayEquals(new double[]{0.5, 0, 0, 0, 0.5}, prediction.toArray());
    }

    void assertArrayEquals(double[] real, double[] actual) {
        assertEquals(real.length, actual.length);
        for (int i = 0; i < real.length; i++) {
            assertEquals(real[i], actual[i], 1e-4);
        }
    }

}
