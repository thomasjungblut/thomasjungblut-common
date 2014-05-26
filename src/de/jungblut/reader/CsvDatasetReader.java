package de.jungblut.reader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Binary dataset reader from CSVs.
 * 
 * @author thomas.jungblut
 * 
 */
public final class CsvDatasetReader {

  private CsvDatasetReader() {
    throw new IllegalAccessError();
  }

  /**
   * Reads a csv into feature and outcome arrays.
   * 
   * @param path the path to read from
   * @param separator the separator to use
   * @param quote the quote, null if none present
   * @param numFeatures the number of features to expect
   * @param outcomeIndex the index of the outcome
   * @return a new dataset.
   */
  public static Dataset readCsv(String path, char separator, Character quote,
      int numFeatures, int outcomeIndex) {
    ArrayList<DoubleVector> featureList = new ArrayList<>();
    ArrayList<DenseDoubleVector> outcomeList = new ArrayList<>();

    try (CSVReader reader = (quote != null ? new CSVReader(
        new FileReader(path), separator, quote) : new CSVReader(new FileReader(
        path), separator))) {

      String[] line;
      while ((line = reader.readNext()) != null) {
        double[] fArray = new double[line.length - 1];
        int index = 0;
        for (int i = 0; i < line.length; i++) {
          if (i != outcomeIndex)
            fArray[index++] = Double.parseDouble(line[i]);
        }
        DoubleVector f = new DenseDoubleVector(fArray);
        DenseDoubleVector o = new DenseDoubleVector(1);
        o.set(0, Double.parseDouble(line[outcomeIndex]));
        featureList.add(f);
        outcomeList.add(o);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    DoubleVector[] features = featureList.toArray(new DoubleVector[featureList
        .size()]);
    DoubleVector[] outcome = outcomeList.toArray(new DoubleVector[outcomeList
        .size()]);
    return new Dataset(features, outcome);

  }
}
