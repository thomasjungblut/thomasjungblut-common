package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.StringPool;

/**
 * Nifty text utility for majorly tokenizing tasks.
 * 
 * @author thomas.jungblut
 * 
 */
public final class TokenizerUtils {

  private TokenizerUtils() {
    throw new IllegalAccessError();
  }

  public static final String SEPARATORS = " \r\n\t.,;:'\"()?!\\-/|“„";
  private static final Pattern SEPARATORS_PATTERN = Pattern
      .compile("[ \r\n\t\\.,;:'\"()?!\\-/|“„]");
  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

  private static final char[] CHARACTER_REPLACE_MAPPING = new char[256];
  static {
    int lowerDifference = 'a' - 'A';

    for (char i = 'A'; i <= 'Z'; i++) {
      CHARACTER_REPLACE_MAPPING[i] = (char) (i + lowerDifference);
    }

    CHARACTER_REPLACE_MAPPING[' '] = ' ';
    CHARACTER_REPLACE_MAPPING['ä'] = 'ä';
    CHARACTER_REPLACE_MAPPING['ö'] = 'ö';
    CHARACTER_REPLACE_MAPPING['ü'] = 'ü';
    CHARACTER_REPLACE_MAPPING['Ä'] = 'ä';
    CHARACTER_REPLACE_MAPPING['Ö'] = 'ö';
    CHARACTER_REPLACE_MAPPING['Ü'] = 'ü';
    CHARACTER_REPLACE_MAPPING['ß'] = 'ß';

    for (char i = '0'; i <= '9'; i++) {
      CHARACTER_REPLACE_MAPPING[i] = i;
    }

    for (char i = 'a'; i <= 'z'; i++) {
      CHARACTER_REPLACE_MAPPING[i] = i;
    }
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
   * q-gram tokenizer, which is basically a proxy to
   * {@link #nShinglesTokenize(String, int)}. These are nGrams based on
   * characters. If you want to use normal word tokenizers, then use
   * {@link #wordTokenize(String)} for unigrams. To generate bigrams out of it
   * you need to call {@link #buildNGrams(String[], int)}.
   * 
   * @param key
   * @param size
   * @return
   */
  public static String[] qGramTokenize(String key, int size) {
    return nShinglesTokenize(key, size);
  }

  /**
   * N-shingles tokenizer. N-Shingles are nGrams based on characters. If you
   * want to use normal word tokenizers, then use {@link #wordTokenize(String)}
   * for unigrams. To generate bigrams out of it you need to call
   * {@link #buildNGrams(String[], int)}.
   */
  public static String[] nShinglesTokenize(String key, int size) {
    if (key.length() < size) {
      return new String[] { key };
    }
    final int listSize = key.length() - size + 1;
    List<String> list = new ArrayList<>(listSize);
    for (int i = 0; i < listSize; i++) {
      int upperBound = i + size;
      list.add(new String(key.substring(i, upperBound)));
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Tokenizes on normal whitespaces "\s" in java regex.
   */
  public static String[] whiteSpaceTokenize(String text) {
    return WHITESPACE_PATTERN.split(text);
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
   * Tokenizes on several indicators of a word, regex is [
   * \r\n\t.,;:'\"()?!\\-/|]
   */
  public static String[] wordTokenize(String text) {
    return SEPARATORS_PATTERN.split(text);
  }

  /**
   * Tokenizes on several indicators of a word, regex to detect these must be
   * given.
   */
  public static String[] wordTokenize(String text, String regex) {
    return text.split(regex);
  }

  /**
   * Normalizes the tokens:<br/>
   * - lower cases <br/>
   * - removes not alphanumeric characters (since I'm german I have included
   * äüöß as well).
   */
  public static String[] normalizeTokens(String[] tokens, boolean removeEmpty) {
    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = normalizeString(tokens[i]);
    }

    if (removeEmpty) {
      tokens = removeEmpty(tokens);
    }
    return tokens;
  }

  /**
   * Normalizes the token:<br/>
   * - lower cases <br/>
   * - removes not alphanumeric characters (since I'm german I have included
   * äüöß as well).
   */
  public static String normalizeString(String token) {
    char[] charArray = token.toCharArray();
    char[] toReturn = new char[charArray.length];
    int index = 0;

    for (int i = 0; i < charArray.length; i++) {
      char x = charArray[i];
      if (x < CHARACTER_REPLACE_MAPPING.length) {
        if (CHARACTER_REPLACE_MAPPING[x] > 0) {
          toReturn[index++] = CHARACTER_REPLACE_MAPPING[x];
        }
      }
    }

    return String.valueOf(Arrays.copyOf(toReturn, index));
  }

  /**
   * Removes empty tokens from given array. The empty slots will be filled with
   * the follow-up tokens.
   */
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
  public static String[] whiteSpaceTokenizeNGrams(String text, int size) {
    String[] whiteSpaceTokenize = whiteSpaceTokenize(text);
    return buildNGrams(whiteSpaceTokenize, size);
  }

  /**
   * This tokenizer uses the given tokens and then concatenates the words based
   * on size.
   */
  public static String[] buildNGrams(String[] tokens, int size) {
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
   * Interns the given strings inplace.
   * 
   * @param strings the strings to intern.
   * @return an interned string array.
   */
  public static String[] internStrings(String[] strings) {
    for (int i = 0; i < strings.length; i++) {
      strings[i] = strings[i].intern();
    }
    return strings;
  }

  /**
   * Interns the given strings inplace with the given pool.
   * 
   * @param strings the strings to intern.
   * @param pool the string pool to use.
   * @return an interned string array.
   */
  public static String[] internStrings(String[] strings, StringPool pool) {
    Preconditions.checkNotNull(pool, "Pool shouldn't be null!");
    for (int i = 0; i < strings.length; i++) {
      strings[i] = pool.pool(strings[i]);
    }
    return strings;
  }

  /**
   * Adds <START> and <END> to the beginning of the array and the end.
   */
  public static String[] addStartAndEndTags(String[] unigram) {
    String[] tmp = new String[unigram.length + 2];
    System.arraycopy(unigram, 0, tmp, 1, unigram.length);
    tmp[0] = "<START>";
    tmp[tmp.length - 1] = "<END>";
    return tmp;
  }

  /**
   * Concats the given tokens with the given delimiter.
   */
  public static String concat(String[] tokens, String delimiter) {
    final int finalIndex = tokens.length - 1;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tokens.length; i++) {
      sb.append(tokens[i]);
      if (i != finalIndex) {
        sb.append(delimiter);
      }
    }
    return sb.toString();
  }

}
