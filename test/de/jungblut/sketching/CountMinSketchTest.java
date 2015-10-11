package de.jungblut.sketching;

import java.util.ArrayList;

import org.junit.Assert;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.nlp.VectorizerUtils;
import de.jungblut.utils.IntegerFunnel;

public class CountMinSketchTest {

  @Test
  public void testMinSketching() {
    HashMultiset<Integer> truth = HashMultiset.create();
    final int numSamples = 100_000;
    CountMinSketch<Integer> sketch = new CountMinSketch<>(100, 10,
        new IntegerFunnel());

    // the mean (50) should be generated most often
    NormalDistribution dist = new NormalDistribution(50d, 5d);
    for (int i = 0; i < numSamples; i++) {
      int val = (int) dist.sample();
      truth.add(val);
      sketch.add(val);
    }

    ArrayList<Entry<Integer>> mostFrequentItems = VectorizerUtils
        .getMostFrequentItems(truth);

    // check that the top 10 items
    for (int i = 0; i < 10; i++) {
      Entry<Integer> entry = mostFrequentItems.get(i);
      int sketchValue = sketch.approximateCount(entry.getElement());

      System.out.println(entry.getElement() + "\t" + entry.getCount() + "\t"
          + sketchValue);

      // since this is a probabilistic structure, values might be slightly off.
      // however, with the given parameters of 100 buckets, we are able to catch
      // the max. 50 distinct items that the gaussian returns pretty easily.
      Assert.assertEquals("element " + entry.getElement()
          + " wasn't expected to be any different!", entry.getCount(),
          sketchValue);

      // TODO: let's write some more tests that assess the error rates
      // for varying sizes of the table width/height and distribution width
    }

  }

}
