package de.jungblut.nlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Nifty text utility for majorly tokenizing tasks.
 * 
 * @author thomas.jungblut
 * 
 */
public final class TokenizerUtils {

  public static final String SEPARATORS = " \r\n\t.,;:'\"()?!\\-";

  /**
   * Fully consumes a lucene tokenstream and returns a string array.
   */
  public static String[] consumeTokenStream(TokenStream stream) {
    ArrayList<String> list = new ArrayList<>();
    try {
      stream.reset();

      CharTermAttribute termAttribute = stream
          .getAttribute(CharTermAttribute.class);
      while (stream.incrementToken()) {
        list.add(termAttribute.toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        stream.end();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        stream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Applies given regex on tokens and may optionally delete when a token gets
   * empty.
   */
  public static String[] removeMatchingRegex(String regex, String replacement,
      String[] tokens, boolean removeEmpty) {
    String[] tk = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      tk[i] = tokens[i].replaceAll(regex, replacement);
    }
    if (removeEmpty) {
      tk = removeEmpty(tk);
    }
    return tk;
  }

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
   * Deduplicates the given tokens, but maintains the order.
   */
  public static String[] deduplicateTokens(String[] tokens) {
    LinkedHashSet<String> set = new LinkedHashSet<>();
    Collections.addAll(set, tokens);
    return set.toArray(new String[set.size()]);
  }

  /**
   * Tokenizes on several indicators of a word, regex is [ \r\n\t.,;:'\"()?!\\-]
   */
  public static String[] wordTokenize(String text) {
    return wordTokenize(text, SEPARATORS);
  }

  public static String[] wordTokenize(String text, String regex) {
    ArrayList<String> list = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(text, regex);
    while (tokenizer.hasMoreElements()) {
      list.add((String) tokenizer.nextElement());
    }
    return list.toArray(new String[list.size()]);
  }

  public static String[] removeEmpty(String[] arr) {
    ArrayList<String> list = new ArrayList<>();
    for (String s : arr) {
      if (s != null && !s.isEmpty())
        list.add(s);
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * This tokenizer first splits on whitespaces and then concatenates the words
   * based on size.
   */
  public static String[] whiteSpaceTokenizeNGramms(String text, int size) {
    String[] whiteSpaceTokenize = whiteSpaceTokenize(text);
    return buildNGramms(whiteSpaceTokenize, size);
  }

  /**
   * This tokenizer uses the given tokens and then concatenates the words based
   * on size.
   */
  public static String[] buildNGramms(String[] tokens, int size) {
    if (tokens.length < size) {
      return tokens;
    }
    List<String> list = new ArrayList<>();
    final int endIndex = tokens.length - size + 1;
    for (int i = 0; i < endIndex; i++) {
      String tkn = tokens[i];
      final int tokenEndIndex = (i + size);
      for (int j = i + 1; j < tokenEndIndex; j++) {
        tkn += " " + tokens[j];
      }
      list.add(tkn);
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Concats the given tokens with the given delimiter.
   */
  public static String concat(String[] tokens, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for (String token : tokens) {
      sb.append(token);
      sb.append(delimiter);
    }
    return sb.toString();
  }

}
