package de.jungblut.nlp;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class LuceneNormalizer implements Normalizer {

  private final Analyzer analyzer;

  public LuceneNormalizer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  @Override
  public String[] tokenizeAndNormalize(String s) {
    TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(s));
    return Tokenizer.consumeTokenStream(tokenStream);
  }

}
