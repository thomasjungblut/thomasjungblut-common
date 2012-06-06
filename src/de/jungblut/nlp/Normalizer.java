package de.jungblut.nlp;

/**
 * Used to normalize specific text documents.
 * 
 * @author thomas.jungblut
 * 
 */
public interface Normalizer {

  /**
   * Tokenizes and normalizes the given string.
   */
  public String[] tokenizeAndNormalize(String s);

}
