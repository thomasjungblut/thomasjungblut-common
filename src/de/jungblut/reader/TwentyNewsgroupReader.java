package de.jungblut.reader;

import gnu.trove.list.array.TDoubleArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple3;

/**
 * Reads the "20news-bydate" dataset into a vector space model as well as
 * predictions based on the category.
 * 
 * @author thomas.jungblut
 * 
 */
public final class TwentyNewsgroupReader {

  private TwentyNewsgroupReader() {
    throw new IllegalAccessError();
  }

  /**
   * Needs the "20news-bydate" directory that has test and train subdirectories
   * given.
   * 
   * @return in tuple3 order: document as string, prediction, name mapping for
   *         prediction
   */
  public static Tuple3<List<String>, DenseDoubleVector, String[]> readTwentyNewsgroups(
      File directory) {
    String[] classList = directory.list();
    Arrays.sort(classList);
    List<String> docList = new ArrayList<>();
    TDoubleArrayList prediction = new TDoubleArrayList();
    String[] nameMapping = new String[classList.length];
    int classIndex = 0;
    for (String classDirString : classList) {
      File classDir = new File(directory, classDirString);
      String[] fileList = classDir.list();
      for (String fileDoc : fileList) {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(
            classDir, fileDoc)))) {
          StringBuilder document = new StringBuilder();
          String l = null;
          while ((l = br.readLine()) != null) {
            document.append(l);
          }
          docList.add(document.toString());
          prediction.add(classIndex);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      nameMapping[classIndex++] = classDirString;
    }

    return new Tuple3<>(docList, new DenseDoubleVector(prediction.toArray()),
        nameMapping);
  }
}
