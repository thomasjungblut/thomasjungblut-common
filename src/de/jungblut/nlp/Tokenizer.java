package de.jungblut.nlp;

import java.util.LinkedList;
import java.util.List;

public final class Tokenizer {

  /**
   * N-Gramm tokenizer.
   */
  public static String[] nGrammTokenize(String key, int size) {
    List<String> list = new LinkedList<>();
    if (key.length() < size) {
      list.add(key);
      return list.toArray(new String[list.size()]);
    }
    for (int i = 0; i < key.length() - size + 1; i++) {
      int upperBound = i + size;
      list.add(key.substring(i, upperBound));
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Tokenizes on normal whitespaces "\s" in java regex.
   */
  public static String[] whiteSpaceTokenize(String text) {
    return text.split("\\s+");
  }

}
