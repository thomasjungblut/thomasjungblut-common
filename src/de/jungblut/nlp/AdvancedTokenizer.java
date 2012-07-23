package de.jungblut.nlp;

/**
 * Advanced tokenizer that lowercases, removes several not needed characters and
 * deduplicates tokens.
 * 
 * @author thomas.jungblut
 * 
 */
public final class AdvancedTokenizer implements Tokenizer {

  @Override
  public String[] tokenize(String toTokenize) {
    String[] wordTokenize = TokenizerUtils.wordTokenize(toTokenize
        .toLowerCase().trim());
    wordTokenize = TokenizerUtils.removeMatchingRegex("[»«\"'®™]", "",
        wordTokenize, true);
    wordTokenize = TokenizerUtils.deduplicateTokens(wordTokenize);
    return wordTokenize;
  }

}
