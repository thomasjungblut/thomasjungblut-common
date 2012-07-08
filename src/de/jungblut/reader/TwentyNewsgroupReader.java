package de.jungblut.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.util.Version;

import de.jungblut.datastructure.StringPool;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.tuple.Tuple3;
import de.jungblut.nlp.Tokenizer;

/**
 * Reads the "20news-bydate" dataset into a vector space model as well as
 * predictions based on the category.
 * 
 * @author thomas.jungblut
 * 
 */
public final class TwentyNewsgroupReader {

  private static final HashSet<String> STOP_WORD_SET = new HashSet<>();
  private static final HashSet<String> PREDEFINED_VOCABULARY = new HashSet<>();
  static {
    for (Object o : EnglishAnalyzer.getDefaultStopSet()) {
      STOP_WORD_SET.add(new String((char[]) o));
    }
    STOP_WORD_SET.addAll(Arrays.asList("from", "subject", "re", "you", "i",
        "stuff", "us", "have", "too", "me", "your", "my"));

    try {
      PREDEFINED_VOCABULARY.addAll(Files.readAllLines(FileSystems.getDefault()
          .getPath("files/vocabulary.txt"), Charset.defaultCharset()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  private static final StringPool HASH_STRING_POOL = StringPool.getPool();
  private static final Analyzer ENGLISH_ANALYZER = new EnglishAnalyzer(
      Version.LUCENE_35, STOP_WORD_SET);

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
          String[] whiteSpaceTokens = Tokenizer
              .consumeTokenStream(ENGLISH_ANALYZER.tokenStream(null,
                  new StringReader(document.toString())));
          for (int i = 0; i < whiteSpaceTokens.length; i++) {
            if (!whiteSpaceTokens[i].matches("\\d+")
                && !STOP_WORD_SET.contains(whiteSpaceTokens[i])
                && PREDEFINED_VOCABULARY.contains(whiteSpaceTokens[i])) {
              whiteSpaceTokens[i] = HASH_STRING_POOL.pool(whiteSpaceTokens[i]);
            } else {
              whiteSpaceTokens[i] = null;
            }
          }
          whiteSpaceTokens = Tokenizer.removeEmpty(whiteSpaceTokens);
          whiteSpaceTokens = Tokenizer.buildNGramms(whiteSpaceTokens, 1);
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
