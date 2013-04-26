package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

public class VectorizerUtilsTest extends TestCase {

  List<String> documents = Lists.newArrayList("this is doc 1", // 0
      "this doc 2", // 1
      "that doc is totally unrelated", // 2
      "i dont think that is a document"); // 3
  List<String[]> tokenizedDocuments = new ArrayList<>(documents.size());
  {
    Tokenizer tkn = new StandardTokenizer();
    for (String doc : documents)
      tokenizedDocuments.add(tkn.tokenize(doc));
  }

  @Test
  public void testBuildDictionary() {
    String[] expectedResults = new String[] { "1", "2", "a", "doc", "document",
        "dont", "i", "is", "that", "think", "this", "totally", "unrelated" };
    String[] dict = VectorizerUtils.buildDictionary(tokenizedDocuments);
    assertArrayEquals(expectedResults, dict);

    // test with spam detector and 50% threshold
    expectedResults = new String[] { "1", "2", "a", "document", "dont", "i",
        "that", "think", "this", "totally", "unrelated" };
    dict = VectorizerUtils.buildDictionary(tokenizedDocuments, 0.5f, 0);
    assertArrayEquals(expectedResults, dict);
  }

  @Test
  public void testBuildInvertedIndexMap() {
    String[] tokens = new String[] { "is", "think", "unrelated", "a", "i", "2",
        "that", "1", "document", "dont", "doc", "totally", "this" };
    int[][] docs = new int[][] { { 0, 2, 3 }, { 3 }, { 2 }, { 3 }, { 3 },
        { 1 }, { 2, 3 }, { 0 }, { 3 }, { 3 }, { 0, 1, 2 }, { 2 }, { 0, 1 }, };
    HashMultimap<String, Integer> invertedIndex = VectorizerUtils
        .buildInvertedIndexMap(tokenizedDocuments,
            VectorizerUtils.buildDictionary(tokenizedDocuments));

    for (int i = 0; i < tokens.length; i++) {
      Set<Integer> set = invertedIndex.get(tokens[i]);
      for (int doc : docs[i]) {
        set.remove(doc);
      }
      assertEquals(set.size(), 0);
    }

  }

  @Test
  public void testBuildInvertedIndexArray() {
    String[] tokens = new String[] { "is", "think", "unrelated", "a", "i", "2",
        "that", "1", "document", "dont", "doc", "totally", "this" };
    int[][] docs = new int[][] { { 0, 2, 3 }, { 3 }, { 2 }, { 3 }, { 3 },
        { 1 }, { 2, 3 }, { 0 }, { 3 }, { 3 }, { 0, 1, 2 }, { 2 }, { 0, 1 }, };
    String[] dict = VectorizerUtils.buildDictionary(tokenizedDocuments);
    int[][] dictDocs = VectorizerUtils.buildInvertedIndexArray(
        tokenizedDocuments, dict);

    for (int i = 0; i < tokens.length; i++) {
      int[] tokenDocs = dictDocs[Arrays.binarySearch(dict, tokens[i])];
      Arrays.sort(docs[i]);
      Arrays.sort(tokenDocs);
      assertArrayEquals(docs[i], tokenDocs);
    }

  }

  @Test
  public void testTfIdfVectorize() {

    String[] dict = VectorizerUtils.buildDictionary(tokenizedDocuments);
    assertEquals(13, dict.length);
    int[] docCount = VectorizerUtils.buildInvertedIndexDocumentCount(
        tokenizedDocuments, dict);

    List<DoubleVector> tfIdfVectorize = VectorizerUtils.tfIdfVectorize(
        tokenizedDocuments, dict, docCount);

    // {10=0.6931471805599453, 7=0.28768207245178085, 3=0.28768207245178085,
    // 0=1.3862943611198906}
    DoubleVector v1 = new SparseDoubleVector(13);
    v1.set(10, 0.6931471805599453);
    v1.set(7, 0.28768207245178085);
    v1.set(3, 0.28768207245178085);
    v1.set(0, 1.3862943611198906);
    // {10=0.6931471805599453, 3=0.28768207245178085, 1=1.3862943611198906}
    DoubleVector v2 = new SparseDoubleVector(13);
    v2.set(10, 0.6931471805599453);
    v2.set(3, 0.28768207245178085);
    v2.set(1, 1.3862943611198906);
    // {12=1.3862943611198906, 11=1.3862943611198906, 8=0.6931471805599453,
    // 7=0.28768207245178085, 3=0.28768207245178085}
    DoubleVector v3 = new SparseDoubleVector(13);
    v3.set(12, 1.3862943611198906);
    v3.set(11, 1.3862943611198906);
    v3.set(8, 0.6931471805599453);
    v3.set(7, 0.28768207245178085);
    v3.set(3, 0.28768207245178085);

    // {9=1.3862943611198906, 8=0.6931471805599453, 7=0.28768207245178085,
    // 6=1.3862943611198906, 5=1.3862943611198906, 4=1.3862943611198906,
    // 2=1.3862943611198906}
    DoubleVector v4 = new SparseDoubleVector(13);
    v4.set(9, 1.3862943611198906);
    v4.set(8, 0.6931471805599453);
    v4.set(7, 0.28768207245178085);
    v4.set(6, 1.3862943611198906);
    v4.set(5, 1.3862943611198906);
    v4.set(4, 1.3862943611198906);
    v4.set(2, 1.3862943611198906);

    assertEquals(4, tfIdfVectorize.size());

    assertEquals(0d, tfIdfVectorize.get(0).subtract(v1).sum(), 1e-5);
    assertEquals(0d, tfIdfVectorize.get(1).subtract(v2).sum(), 1e-5);
    assertEquals(0d, tfIdfVectorize.get(2).subtract(v3).sum(), 1e-5);
    assertEquals(0d, tfIdfVectorize.get(3).subtract(v4).sum(), 1e-5);

  }

  @Test
  public void testPointwiseMutualInformationVectorize() {
    // TODO
  }

  @Test
  public void testGetMostFrequentItems() {
    Map<String, Integer> expectedResults = new HashMap<>();
    String[] expectedResultsArray = new String[] { "is", "doc", "that", "this",
        "think", "unrelated", "a", "i", "2", "1", "document", "dont", "totally" };
    int[] expectedResultCounts = new int[] { 3, 3, 2, 2, 1, 1, 1, 1, 1, 1, 1,
        1, 1 };
    for (int i = 0; i < expectedResultCounts.length; i++) {
      expectedResults.put(expectedResultsArray[i], expectedResultCounts[i]);
    }

    HashMultiset<String> set = HashMultiset.create();
    for (String[] s : tokenizedDocuments)
      set.addAll(Arrays.asList(s));
    ArrayList<Entry<String>> mostFrequentItems = VectorizerUtils
        .getMostFrequentItems(set);

    for (Entry<String> entry : mostFrequentItems) {
      assertEquals(expectedResults.get(entry.getElement()).intValue(),
          entry.getCount());
      expectedResults.remove(entry.getElement());
    }
    assertEquals(0, expectedResults.size());
  }

  static void assertArrayEquals(int[] expected, int[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

  static void assertArrayEquals(String[] expected, String[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

}
