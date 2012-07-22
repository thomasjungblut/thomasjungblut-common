package de.jungblut.nlp;

/**
 * Just a basic tokenizer by whitespaces.
 * 
 * @author thomas.jungblut
 * 
 */
public final class StandardTokenizer implements Tokenizer {

  @Override
  public String[] tokenize(String toTokenize) {
    return TokenizerUtils.whiteSpaceTokenize(toTokenize);
  }

}
