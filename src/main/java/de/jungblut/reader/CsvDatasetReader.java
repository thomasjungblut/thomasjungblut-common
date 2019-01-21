package de.jungblut.reader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.dense.SingleEntryDoubleVector;

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
   * @param outcomeIndex the index of the outcome, everything else is considered
   *          a feature.
   * @param skipHeader if true it will skip parsing the first line.
   * @param cacheOutcomeVectors if true it caches the vectors based on the
   *          outcome value. This saves a ton of memory for classification
   *          problems that share only a couple of unique values.
   * @return a new dataset.
   */
  @SuppressWarnings("resource")
  public static Dataset readCsv(String path, char separator, Character quote,
      int outcomeIndex, boolean skipHeader, boolean cacheOutcomeVectors) {
    ArrayList<DoubleVector> featureList = new ArrayList<>();
    ArrayList<DoubleVector> outcomeList = new ArrayList<>();

    HashMap<Integer, SingleEntryDoubleVector> cacheMap = new HashMap<>();

    try (FileReader fr = new FileReader(path)) {
      CSVReader reader = null;
      if (quote == null) {
        reader = new CSVReader(fr, separator);
      } else {
        reader = new CSVReader(fr, separator, quote);
      }

      if (skipHeader) {
        reader.readNext();
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
        featureList.add(f);
        SingleEntryDoubleVector o = new SingleEntryDoubleVector(
            Double.parseDouble(line[outcomeIndex]));

        if (cacheOutcomeVectors) {
          // assume we can discretize here into an integer
          int key = (int) o.get(0);
          SingleEntryDoubleVector cached = cacheMap.get(key);
          if (cached == null) {
            cacheMap.put(key, o);
          } else {
            o = cached;
          }
        }

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
