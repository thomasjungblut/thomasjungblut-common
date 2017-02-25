package de.jungblut.nlp;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.datastructure.StringPool;

/**
 * Nifty text utility for majorly tokenizing tasks.
 * 
 * @author thomas.jungblut
 * 
 */
public final class TokenizerUtils {

  public static final String END_TAG = "<END>";
  public static final String START_TAG = "<START>";
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

  private static final String[] EMOTICON_STRINGS = new String[] { "^^", "<3",
      "@", "*", "❤", "☺", "™" };

  private static final Pattern NUMERIC_PATTERN = Pattern.compile("[0-9]");
  private static final CharSequence NON_BREAKING_WHITESPACE = ((char) 160) + "";

  private TokenizerUtils() {
    throw new IllegalAccessError();
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
   * Tokenizes on normal whitespaces "\\s+" in java regex.
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
    return wordTokenize(text, false);
  }

  /**
   * Tokenizes like {@link #wordTokenize(String)} does, but keeps the seperators
   * as their own token if the argument is true.
   */
  public static String[] wordTokenize(String text, boolean keepSeperators) {
    if (keepSeperators) {
      StringTokenizer tkns = new StringTokenizer(text, SEPARATORS, true);
      int countTokens = tkns.countTokens();
      String[] toReturn = new String[countTokens];
      int i = 0;
      while (countTokens-- > 0) {
        toReturn[i] = tkns.nextToken();
        if (toReturn[i].charAt(0) > ' ') {
          i++;
        }
      }
      return Arrays.copyOf(toReturn, i);
    } else {
      return SEPARATORS_PATTERN.split(text);
    }
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
      StringBuilder tkn = new StringBuilder(tokens[i]);
      final int tokenEndIndex = (i + size);
      for (int j = i + 1; j < tokenEndIndex; j++) {
        tkn.append(' ');
        tkn.append(tokens[j]);
      }
      list.add(tkn.toString());
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Builds ngrams from a range of tokens, basically a concat of all the
   * {@link #buildNGrams(String[], int)} calls within the range. Both start and
   * end are inclusive.
   */
  public static String[] buildNGramsRange(String[] tokens, int startSize,
      int endSize) {

    String[] tkn = buildNGrams(tokens, startSize);
    for (int i = startSize + 1; i <= endSize; i++) {
      tkn = ArrayUtils.concat(tkn, buildNGrams(tokens, i));
    }

    return tkn;
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
    tmp[0] = START_TAG;
    tmp[tmp.length - 1] = END_TAG;
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

  /**
   * Replaces all numerics with "#".
   */
  public static String[] numericsToHash(String[] tokens) {
    String[] toReturn = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      toReturn[i] = NUMERIC_PATTERN.matcher(tokens[i]).replaceAll("#");
    }
    return toReturn;
  }

  /**
   * Splits the given token into a possibly larger sequence so that every
   * emoticon is its own token.
   * 
   * TODO this needs some really comprehensive tests
   */
  public static String[] emoticonSplit(String[] tokens) {
    ArrayList<String> toReturn = new ArrayList<>();
    for (String token : tokens) {
      boolean splitted = false;
      for (String pattern : EMOTICON_STRINGS) {
        if (token.contains(pattern)) {
          toReturn.addAll(splitOnToken(token, pattern));
          splitted = true;
          break;
        } else if (containsEmoticon(token)) {
          toReturn.addAll(splitEmoticon(token));
          splitted = true;
          break;
        }
      }

      if (!splitted) {
        toReturn.add(token);
      }
    }

    return toReturn.toArray(new String[toReturn.size()]);
  }

  private static List<String> splitEmoticon(String token) {
    ArrayList<String> list = new ArrayList<>();

    char[] charArray = token.toCharArray();
    int offset = 0;
    for (int i = 0; i < charArray.length; i++) {
      int emoticonCharLen = emoticonCharLen(token, i);
      if (emoticonCharLen > 0) {
        // everything up to here is its own string, this char is a string and
        // the rest needs to be parsed seperately
        if (offset != i) {
          list.add(new String(Arrays.copyOfRange(charArray, offset, i)));
        }
        list.add(new String(Arrays.copyOfRange(charArray, i, i
            + emoticonCharLen)));
        offset = i + emoticonCharLen;
        i += emoticonCharLen - 1;
      }
    }

    if (offset < token.length()) {
      list.add(new String(Arrays.copyOfRange(charArray, offset, token.length())));
    }

    return list;
  }

  private static boolean containsEmoticon(String token) {
    for (int i = 0; i < token.length(); i++) {
      if (emoticonCharLen(token, i) > 0) {
        return true;
      }
    }
    return false;
  }

  private static int emoticonCharLen(String s, int idx) {
    if (idx < 0 || idx >= s.length() || idx + 1 >= s.length()) {
      return -1;
    }

    if (Character.UnicodeBlock.EMOTICONS == UnicodeBlock.of(s.charAt(idx))) {
      return 1;
    }

    char start = s.charAt(idx);
    char end = s.charAt(idx + 1);
    if (Character.isSurrogatePair(start, end)) {
      int codePoint = Character.toCodePoint(start, end);
      if (Character.UnicodeBlock.EMOTICONS == UnicodeBlock.of(codePoint)) {
        return 2;
      }
    }

    return -1;
  }

  private static List<String> splitOnToken(String token, String splitPattern) {
    if (token.length() == 0) {
      return Collections.emptyList();
    }

    int idx = token.indexOf(splitPattern);
    if (idx >= 0) {
      if (token.equals(splitPattern)) {
        return Collections.singletonList(token);
      }

      ArrayList<String> list = new ArrayList<>();
      int end = idx + splitPattern.length();
      list.add(token.substring(idx, end));
      // search for splits in the rest of the tokens
      String rest = token.substring(end);
      list.addAll(splitOnToken(rest, splitPattern));

      return list;
    }
    return Collections.emptyList();
  }

  /**
   * Trims the tokens using {@link String#trim()} and additionally removes
   * non-breaking spaces.
   */
  public static String[] trim(String[] tokens) {
    String[] toReturn = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      // removes spaces and non-breaking spaces
      toReturn[i] = tokens[i].trim().replace(NON_BREAKING_WHITESPACE, "");
    }
    return toReturn;
  }

}
