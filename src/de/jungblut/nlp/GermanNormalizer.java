package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

public class GermanNormalizer implements Normalizer {

  /**
   * TODO this should be configurable: <br/>
   * - which stop words <br/>
   * - how long tokens to remove <br/>
   * - merge with follow-up <br/>
   * - the tokenizer to use <br/>
   * - remove punctionation
   */

  private static final Pattern specialSignFilter = Pattern.compile(
      "\\p{Punct}", Pattern.CASE_INSENSITIVE);

  public final static HashSet<String> GERMAN_STOP_WORDS = new HashSet<String>(
      Arrays.asList(new String[] { "and", "the", "of", "to", "einer", "eine",
          "eines", "einem", "einen", "der", "die", "das", "dass", "da\u00DF",
          "du", "er", "sie", "es", "was", "wer", "wie", "wir", "und", "oder",
          "ohne", "mit", "am", "im", "in", "aus", "auf", "ist", "sein", "war",
          "wird", "ihr", "ihre", "ihres", "ihnen", "ihrer", "als", "f\u00FCr",
          "von", "mit", "dich", "dir", "mich", "mir", "mein", "sein", "kein",
          "durch", "wegen", "wird", "sich", "bei", "beim", "noch", "den",
          "dem", "zu", "zur", "zum", "auf", "ein", "auch", "werden", "an",
          "des", "sein", "sind", "vor", "nicht", "sehr", "um", "unsere",
          "ohne", "so", "da", "nur", "diese", "dieser", "diesem", "dieses",
          "nach", "\u00FCber", "mehr", "hat", "bis", "uns", "unser", "unserer",
          "unserem", "unsers", "euch", "euers", "euer", "eurem", "ihr",
          "ihres", "ihrer", "ihrem", "alle", "vom" }));

  @Override
  public String[] tokenizeAndNormalize(String s) {
    final String trim = s.toLowerCase().trim();
    final String replaceAll = specialSignFilter.matcher(trim).replaceAll("");
    String[] tokens = Tokenizer.whiteSpaceTokenize(replaceAll);
    ArrayList<String> list = new ArrayList<String>();
    for (int i = 0; i < tokens.length; i++) {
      if (!tokens[i].isEmpty() && !isWhitespaceToken(tokens[i])
          && !GERMAN_STOP_WORDS.contains(tokens[i])) {
        list.add(tokens[i]);
      }
    }

    list = mergeDigitsWithFollowUpToken(list);
    // remove all tokens that are longer than 20
    Iterator<String> it = list.iterator();
    while (it.hasNext()) {
      if (it.next().length() > 20) {
        it.remove();
      }
    }

    return list.toArray(new String[0]);
  }

  private final ArrayList<String> mergeDigitsWithFollowUpToken(
      ArrayList<String> list) {
    ArrayList<String> toReturn = new ArrayList<String>();
    Iterator<String> it = list.iterator();
    ArrayList<String> buffer = new ArrayList<String>();
    while (it.hasNext()) {
      final String next = it.next();
      if (isDigitToken(next)) {
        buffer.add(next);
      } else {
        if (buffer.isEmpty()) {
          toReturn.add(next);
        } else {
          buffer.add(next);
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < buffer.size(); i++) {
            sb.append(buffer.get(i));
            if (i != buffer.size() - 1)
              sb.append("_");
          }
          toReturn.add(sb.toString());
          buffer.clear();
        }
      }
    }

    return toReturn;
  }

  protected final boolean isDigitToken(String token) {
    char[] chars = token.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (!Character.isDigit(chars[i]))
        return false;
    }
    return true;
  }

  protected final boolean isWhitespaceToken(String token) {
    char[] chars = token.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (!Character.isWhitespace(chars[i]))
        return false;
    }
    return true;
  }

}
