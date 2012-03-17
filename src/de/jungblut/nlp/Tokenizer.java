package de.jungblut.nlp;

import java.util.LinkedList;
import java.util.List;

public final class Tokenizer {

  /**
   * N-Gramm tokenizer.
   */
  public static String[] nGrammTokenize(String key, int size) {
    if (key.length() < size) {
      return new String[] { key };
    }
    List<String> list = new LinkedList<>();
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

  /**
   * This tokenizer first splits on whitespaces and then concatenates the words
   * based on size.
   */
  public static String[] whiteSpaceTokenizeNGramms(String text, int size) {
    String[] whiteSpaceTokenize = whiteSpaceTokenize(text);
    return whiteSpaceTokenizeNGramms(whiteSpaceTokenize, size);
  }

  /**
   * This tokenizer uses the given tokens and then concatenates the words based
   * on size.
   */
  public static String[] whiteSpaceTokenizeNGramms(String[] tokens, int size) {
    if (tokens.length < size) {
      return tokens;
    }
    List<String> list = new LinkedList<>();
    for (int i = 0; i < tokens.length - 1; i++) {
      list.add(tokens[i] + " " + tokens[i + 1]);
    }
    return list.toArray(new String[list.size()]);
  }

}
