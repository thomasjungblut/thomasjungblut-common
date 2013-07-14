package de.jungblut.nlp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TokenizerUtilsTest {

  @Test
  public void testRemoveMatchingRegex() {
    String[] tokens = new String[] { "abc", "123", "xyz" };
    String[] desiredResult = new String[] { "", "123", "xyz" };
    String[] result = TokenizerUtils.removeMatchingRegex("[abc]", "", tokens,
        false);

    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredResult[i], result[i]);
    }

    result = TokenizerUtils.removeMatchingRegex("[abc]", "", tokens, true);

    assertEquals(2, result.length);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredResult[i + 1], result[i]);
    }
  }

  @Test
  public void testNGrammTokenize() {
    String s = "Hi this is a test for the tokenizer function!";
    String[] desiredResult = new String[] { "Hi ", "i t", " th", "thi", "his",
        "is ", "s i", " is", "is ", "s a", " a ", "a t", " te", "tes", "est",
        "st ", "t f", " fo", "for", "or ", "r t", " th", "the", "he ", "e t",
        " to", "tok", "oke", "ken", "eni", "niz", "ize", "zer", "er ", "r f",
        " fu", "fun", "unc", "nct", "cti", "tio", "ion", "on!" };
    String[] result = TokenizerUtils.nShinglesTokenize(s, 3);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredResult[i], result[i]);
    }
  }

  @Test
  public void testWhiteSpaceTokenize() {
    String s = "Hi this is a test for the tokenizer function!";
    String[] desiredResult = new String[] { "Hi", "this", "is", "a", "test",
        "for", "the", "tokenizer", "function!" };
    String[] result = TokenizerUtils.whiteSpaceTokenize(s);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredResult[i], result[i]);
    }
  }

  @Test
  public void testDeduplicateTokens() {
    String[] s = new String[] { "Hi", "this", "is", "is", "a", "test", "for",
        "the", "tokenizer", "function!" };
    String[] desiredResult = new String[] { "Hi", "this", "is", "a", "test",
        "for", "the", "tokenizer", "function!" };
    String[] result = TokenizerUtils.deduplicateTokens(s);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredResult[i], result[i]);
    }
  }

  @Test
  public void testWordTokenize() {
    String s = "Hi\nthis\tis.a,test for the tokenizer?function";
    String[] desiredResult = new String[] { "Hi", "this", "is", "a", "test",
        "for", "the", "tokenizer", "function" };
    String[] result = TokenizerUtils.wordTokenize(s);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredResult[i], result[i]);
    }
  }

  @Test
  public void testRemoveEmpty() {
    String[] tokens = new String[] { "", "123", "xyz" };
    String[] desiredResult = new String[] { "123", "xyz" };
    String[] result = TokenizerUtils.removeEmpty(tokens);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredResult[i], result[i]);
    }
  }

  @Test
  public void testWhiteSpaceTokenizeNGramms() {
    String s = "Hi this is a test for the tokenizer function!";
    String[] desiredUniGramResult = new String[] { "Hi", "this", "is", "a",
        "test", "for", "the", "tokenizer", "function!" };
    String[] desiredBiGramResult = new String[] { "Hi this", "this is", "is a",
        "a test", "test for", "for the", "the tokenizer", "tokenizer function!" };
    String[] desiredTriGramResult = new String[] { "Hi this is", "this is a",
        "is a test", "a test for", "test for the", "for the tokenizer",
        "the tokenizer function!" };

    String[] result = TokenizerUtils.whiteSpaceTokenizeNGrams(s, 1);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredUniGramResult[i], result[i]);
    }

    result = TokenizerUtils.whiteSpaceTokenizeNGrams(s, 2);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredBiGramResult[i], result[i]);
    }

    result = TokenizerUtils.whiteSpaceTokenizeNGrams(s, 3);
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredTriGramResult[i], result[i]);
    }
  }

  @Test
  public void testConcat() {
    String result = "Hi,this,is,a,test,for,the,tokenizer,function!";
    String[] tokens = new String[] { "Hi", "this", "is", "a", "test", "for",
        "the", "tokenizer", "function!" };
    String concat = TokenizerUtils.concat(tokens, ",");
    assertEquals(result, concat);
  }

  @Test
  public void testAddStartAndEndTags() {
    String[] uniGram = new String[] { "Hi", "this", "is", "a", "test", "for",
        "the", "tokenizer", "function!" };
    String[] result = TokenizerUtils.addStartAndEndTags(uniGram);
    String[] desiredUniGramResult = new String[] { "<START>", "Hi", "this",
        "is", "a", "test", "for", "the", "tokenizer", "function!", "<END>" };
    for (int i = 0; i < result.length; i++) {
      assertEquals(desiredUniGramResult[i], result[i]);
    }

  }

}
