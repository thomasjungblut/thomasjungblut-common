package de.jungblut.classification.bayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.nlp.Tokenizer;
import de.jungblut.util.Tuple3;

public class TwentyNewsgroupReader {

  // docs, prediction, name mapping for prediction
  public static Tuple3<List<String[]>, DenseIntVector, String[]> readTwentyNewsgroups(
      File directory) {
    List<String[]> docList = new ArrayList<String[]>();
    List<Integer> prediction = new ArrayList<Integer>();
    String[] leastSeenNameMapping = null;
    for (File trainDirectory : directory.listFiles()) {
      String[] classList = trainDirectory.list();
      Arrays.sort(classList);
      String[] nameMapping = new String[classList.length];
      int classIndex = 0;
      for (String classDirString : classList) {
        File classDir = new File(trainDirectory, classDirString);
        String[] fileList = classDir.list();
        for (String fileDoc : fileList) {
          try (BufferedReader br = new BufferedReader(new FileReader(new File(
              classDir, fileDoc)))) {
            StringBuilder document = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
              document.append(line);
            }
            String[] tokens = Tokenizer.whiteSpaceTokenize(document.toString());
            docList.add(tokens);
            prediction.add(classIndex);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        nameMapping[classIndex++] = classDirString;
      }
      leastSeenNameMapping = nameMapping;
    }

    return new Tuple3<List<String[]>, DenseIntVector, String[]>(docList,
        new DenseIntVector(prediction), leastSeenNameMapping);
  }
}
