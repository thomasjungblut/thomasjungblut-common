package de.jungblut.nlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class Tokenizer {

  /**
   * Fully consumes a lucene tokenstream and returns a string array.
   * 
   * @throws IOException
   */
  public static String[] consumeTokenStream(TokenStream stream) {
    ArrayList<String> list = new ArrayList<String>();
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
   * Tokenizes on several indicators of a word, regex is [ \r\n\t.,;:'\"()?!]
   */
  public static String[] wordTokenize(String text) {
    return text.split("[ \r\n\t.,;:'\"()?!]");
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
