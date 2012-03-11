package de.jungblut.nlp;

import java.util.LinkedList;
import java.util.List;

public final class Tokenizer {

  // basic n-gram tokenizer
  public static String[] tokenize(String key, int size) {
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

}
