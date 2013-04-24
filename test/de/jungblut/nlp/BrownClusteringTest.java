package de.jungblut.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.collect.Sets;

public class BrownClusteringTest extends TestCase {

  String[] documents = { "the cat chased the mouse", "the dog chased the cat",
      "the mouse chased the dog" };
  List<String[]> docs = new ArrayList<>();

  @Override
  protected void setUp() throws Exception {
    for (String doc : documents) {
      docs.add(TokenizerUtils.wordTokenize(doc.toLowerCase().trim()));
    }
  }

  @Test
  public void testBrownClustering() {
    List<Set<String>> clusters = BrownClustering.cluster(docs, 2);
    Set<String> allWords = Sets.newHashSet("cat", "dog", "the", "chased",
        "mouse");
    Set<String> animalCluster = Sets.newHashSet("cat", "dog", "mouse");
    for (Set<String> set : clusters) {
      for (String s : set) {
        allWords.remove(s);
      }
      animalCluster.removeAll(set);
    }
    assertEquals(0, allWords.size());
    assertEquals(0, animalCluster.size());
  }
}
