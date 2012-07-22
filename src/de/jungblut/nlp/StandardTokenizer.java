package de.jungblut.nlp;

public final class StandardTokenizer implements Tokenizer {

  @Override
  public String[] tokenize(String toTokenize) {
    return TokenizerUtils.whiteSpaceTokenize(toTokenize);
  }

}
