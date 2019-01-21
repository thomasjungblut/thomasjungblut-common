package de.jungblut.nlp;

/**
 * Advanced tokenizer that lowercases, adds start and end tags, deduplicates
 * tokens and builds bigrams.
 * 
 * @author thomas.jungblut
 * 
 */
public final class BigramTokenizer implements Tokenizer {

  @Override
  public String[] tokenize(String toTokenize) {
    String[] wordTokenize = TokenizerUtils.wordTokenize(toTokenize
        .toLowerCase().trim());
    wordTokenize = TokenizerUtils.addStartAndEndTags(wordTokenize);
    wordTokenize = TokenizerUtils.deduplicateTokens(wordTokenize);
    wordTokenize = TokenizerUtils.buildNGrams(wordTokenize, 2);
    return wordTokenize;
  }

}
