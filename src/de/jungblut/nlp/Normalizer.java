package de.jungblut.nlp;

/**
 * Used to normalize specific text documents.
 * 
 * @author thomas.jungblut
 * 
 */
public interface Normalizer {

  public String[] tokenizeAndNormalize(String s);

}
