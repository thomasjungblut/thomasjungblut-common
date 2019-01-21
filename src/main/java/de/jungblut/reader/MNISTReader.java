package de.jungblut.reader;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MNIST CSV reader from kaggle: www.kaggle.com/c/digit-recognizer/
 *
 * @author thomas.jungblut
 */
public final class MNISTReader {

    private MNISTReader() {
        throw new IllegalAccessError();
    }

    public static Dataset readMNISTTrainImages(String path) {
        List<DoubleVector> features = new ArrayList<>();
        List<DoubleVector> prediction = new ArrayList<>();

        DoubleVector[] classes = new DoubleVector[10];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = new DenseDoubleVector(classes.length);
            classes[i].set(i, 1d);
        }

        String line = null;
        int numLine = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            while ((line = br.readLine()) != null) {
                if (numLine == 0) {
                    numLine++;
                    continue;
                }

                String[] split = line.split(",");
                DenseDoubleVector featureVector = new DenseDoubleVector(
                        split.length - 1);
                for (int i = 1; i < split.length; i++) {
                    featureVector.set(i - 1, Integer.parseInt(split[i]));
                }

                DoubleVector predVector = classes[Integer.parseInt(split[0])];

                features.add(featureVector);
                prediction.add(predVector);
                numLine++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Dataset(features.toArray(new DoubleVector[features.size()]),
                prediction.toArray(new DenseDoubleVector[features.size()]));
    }
}
