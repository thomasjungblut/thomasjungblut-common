package de.jungblut.datastructure;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import de.jungblut.datastructure.InvertedIndex.DocumentDistanceMeasurer;
import de.jungblut.datastructure.InvertedIndex.DocumentMapper;
import de.jungblut.distance.CosineDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;
import de.jungblut.nlp.TokenizerUtils;

public class InvertedIndexTest {

  private static final List<String> phrases = Arrays.asList("I eat the dog",
      "You like the dog", "this is the best I have ever seen");

  @Test
  public void testInvertedIndex() {

    InvertedIndex<String, String> invIndex = getBuiltIndex();

    List<DistanceResult<String>> res = invIndex
        .query("something with the dog I like");
    assertEquals(3, res.size());
    assertEquals(phrases.get(1), res.get(0).get());
    assertEquals(phrases.get(0), res.get(1).get());
    assertEquals(phrases.get(2), res.get(2).get());

    res = invIndex.query("something with the dog I like", 0.8d);
    assertEquals(2, res.size());
    assertEquals(phrases.get(1), res.get(0).get());
    assertEquals(phrases.get(0), res.get(1).get());

    res = invIndex.query("something with the dog I like", 1, 0.8d);
    assertEquals(1, res.size());
    assertEquals(phrases.get(1), res.get(0).get());

    res = invIndex.query("something with the dog I like", 1, 0.8d);
    assertEquals(1, res.size());
    assertEquals(phrases.get(1), res.get(0).get());

    res = invIndex.query("something with the dog I like", 1, 0.5d);
    assertEquals(0, res.size());
  }

  @Test
  public void testVectorInvertedIndex() {

    InvertedIndex<DoubleVector, Integer> invIndex = InvertedIndex
        .createVectorIndex(new CosineDistance());
    DoubleVector v1 = new SparseDoubleVector(4);
    v1.set(1, 0.6931471805599453);
    v1.set(0, 1.3862943611198906);
    DoubleVector v2 = new SparseDoubleVector(4);
    v2.set(2, 0.6931471805599453);
    v2.set(1, 1.3862943611198906);

    invIndex.build(Arrays.asList(v1, v2));

    DoubleVector v3 = new SparseDoubleVector(4);
    v3.set(3, 0.2);
    v3.set(1, 1);
    List<DistanceResult<DoubleVector>> res = invIndex.query(v3);
    assertEquals(2, res.size());
    assertEquals(v2, res.get(0).get());
    assertEquals(v1, res.get(1).get());

  }

  public static InvertedIndex<String, String> getBuiltIndex() {
    // create a white space tokenizing index that measures the jaccard
    // distance.
    InvertedIndex<String, String> invIndex = InvertedIndex.create(
        new DocumentMapper<String, String>() {
          @Override
          public Set<String> mapDocument(String doc) {
            return Sets.newHashSet(TokenizerUtils.whiteSpaceTokenize(doc));
          }
        }, new DocumentDistanceMeasurer<String, String>() {
          @Override
          public double measure(String reference, Set<String> referenceKeys,
              String doc, Set<String> docKeys) {
            SetView<String> union = Sets.union(referenceKeys, docKeys);
            SetView<String> intersection = Sets.intersection(referenceKeys,
                docKeys);
            return 1d - (intersection.size() / (double) union.size());
          }
        });

    invIndex.build(phrases);
    return invIndex;
  }
}
