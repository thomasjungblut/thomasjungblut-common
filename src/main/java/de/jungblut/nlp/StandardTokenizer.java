package de.jungblut.nlp;

/**
 * Just a basic tokenizer by certain attributes with normalization.
 *
 * @author thomas.jungblut
 */
public final class StandardTokenizer implements Tokenizer {

    @Override
    public String[] tokenize(String toTokenize) {
        return TokenizerUtils.normalizeTokens(
                TokenizerUtils.wordTokenize(toTokenize), true);
    }

}
