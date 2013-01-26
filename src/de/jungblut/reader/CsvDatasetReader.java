package de.jungblut.reader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Binary dataset reader from CSVs.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class CsvDatasetReader {

  /**
   * Reads a csv into feature and outcome arrays.
   * 
   * @param path the path to read from
   * @param separator the separator to use
   * @param quote the quote, null if none present
   * @param numFeatures the number of features to expect
   * @param outcomeIndex the index of the outcome
   * @return a tuple with features and outcomes.
   */
  public static Tuple<DoubleVector[], DenseDoubleVector[]> readCsv(String path,
      char separator, Character quote, int numFeatures, int outcomeIndex) {
    CSVReader reader = null;
    ArrayList<DoubleVector> featureList = new ArrayList<>();
    ArrayList<DenseDoubleVector> outcomeList = new ArrayList<>();
    try {
      if (quote != null) {
        reader = new CSVReader(new FileReader(path), separator, quote);
      } else {
        reader = new CSVReader(new FileReader(path), separator);
      }
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
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    DoubleVector[] features = featureList.toArray(new DoubleVector[featureList
        .size()]);
    DenseDoubleVector[] outcome = outcomeList
        .toArray(new DenseDoubleVector[outcomeList.size()]);
    return new Tuple<DoubleVector[], DenseDoubleVector[]>(features, outcome);

  }
}
