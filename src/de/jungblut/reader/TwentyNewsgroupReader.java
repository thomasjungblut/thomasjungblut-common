package de.jungblut.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jungblut.datastructure.StringPool;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.tuple.Tuple3;
import de.jungblut.nlp.TokenizerUtils;

/**
 * Reads the "20news-bydate" dataset into a vector space model as well as
 * predictions based on the category.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class TwentyNewsgroupReader {

  private static final StringPool HASH_STRING_POOL = StringPool.getPool();

  /**
   * Needs the "20news-bydate" directory that has test and train subdirectories
   * given.
   * 
   * @return in tuple3 order: docs, prediction, name mapping for prediction
   */
  public static Tuple3<List<String[]>, DenseIntVector, String[]> readTwentyNewsgroups(
      File directory) {
    String[] classList = directory.list();
    Arrays.sort(classList);
    List<String[]> docList = new ArrayList<>();
    List<Integer> prediction = new ArrayList<>();
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
          String[] whiteSpaceTokens = TokenizerUtils
              .whiteSpaceTokenize(document.toString());
          whiteSpaceTokens = TokenizerUtils.removeEmpty(whiteSpaceTokens);
          whiteSpaceTokens = TokenizerUtils.buildNGramms(whiteSpaceTokens, 2);
          for (int i = 0; i < whiteSpaceTokens.length; i++) {
            whiteSpaceTokens[i] = HASH_STRING_POOL.pool(whiteSpaceTokens[i]);
          }
          docList.add(whiteSpaceTokens);
          prediction.add(classIndex);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      nameMapping[classIndex++] = classDirString;
    }

    return new Tuple3<>(docList, new DenseIntVector(prediction), nameMapping);
  }
}
