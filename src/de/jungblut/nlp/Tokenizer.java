package de.jungblut.nlp;

/**
 * Standard tokenizer interface. Makes multiple strings out of one.
 * 
 * @author thomas.jungblut
 * 
 */
public interface Tokenizer {

  /**
   * Tokenizes the given String to a array of Strings.
   * 
   * @param toTokenize the string to tokenize.
   * @return the array of tokenized tokens.
   */
  public String[] tokenize(String toTokenize);

}
