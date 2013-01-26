package de.jungblut.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;

/**
 * Dataset vectorizer for the mushroom dataset. Parses the nominal values into
 * incremented numbers beginning from 0.
 * 
 */
public abstract class MushroomReader {

  private static final String MUSHROOM_DATASET_PATH = "files/mushroom/mushroom_dataset.csv";

  /**
   * @return a tuple, on first dimension are the features, on the second are the
   *         outcomes (0 or 1 in the first element of a vector)
   */
  public static Tuple<DoubleVector[], DenseDoubleVector[]> readMushroomDataset() {
    List<DoubleVector> list = new ArrayList<>();
    List<String[]> buffer = new ArrayList<>();
    HashMultimap<Integer, String> multiMap = HashMultimap.create();
    try (BufferedReader br = new BufferedReader(new FileReader(
        MUSHROOM_DATASET_PATH))) {
      int numLines = 0;
      String line = null;
      while ((line = br.readLine()) != null) {
        // skip the header
        if (numLines == 0) {
          numLines++;
          continue;
        }
        String[] split = line.split(",");
        Preconditions.checkArgument(split.length == 23,
            "CSV length was not 23! Given " + split.length);
        for (int i = 0; i < split.length; i++) {
          multiMap.put(i, split[i]);
        }
        buffer.add(split);
        numLines++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // now map each string at each index of a line to a integer
    HashMap<Integer, HashMap<String, Integer>> indexMapping = new HashMap<>();
    Set<Integer> keySet = multiMap.keySet();
    for (int index : keySet) {
      HashMap<String, Integer> featureMap = new HashMap<>();
      Set<String> set = multiMap.get(index);
      int nameIndex = 0;
      for (String name : set) {
        featureMap.put(name, nameIndex++);
      }
      indexMapping.put(index, featureMap);
    }

    // now we have the mappings, we can loop again over all lines
    for (String[] line : buffer) {
      DoubleVector vec = new DenseDoubleVector(line.length);
      for (int i = 0; i < line.length; i++) {
        HashMap<String, Integer> hashMap = indexMapping.get(i);
        Integer value = hashMap.get(line[i]);
        vec.set(i, value);
      }
      // and add the mappings to the vectorlist
      list.add(vec);
    }

    DoubleVector[] features = new DoubleVector[list.size()];
    DenseDoubleVector[] outcome = new DenseDoubleVector[list.size()];
    for (int i = 0; i < list.size(); i++) {
      DoubleVector doubleVector = list.get(i);
      features[i] = doubleVector.slice(doubleVector.getLength() - 1);
      outcome[i] = (DenseDoubleVector) doubleVector.slice(
          doubleVector.getLength() - 1, doubleVector.getLength());
    }

    return new Tuple<>(features, outcome);
  }
}
