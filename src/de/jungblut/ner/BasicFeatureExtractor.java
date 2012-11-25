package de.jungblut.ner;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic feature extraction for sequence learning, takes the current word into
 * account and the previous label - as well as the joint version of both. This
 * will implicitly create a dictionary in your features.
 * 
 * @author thomas.jungblut
 * 
 */
public final class BasicFeatureExtractor implements SequenceFeatureExtractor {

  @Override
  public List<String> computeFeatures(List<String> words, List<Integer> labels,
      int position) {
    String currentWord = words.get(position);
    int previousLabel = position == 0 ? 0 : labels.get(position - 1);
    List<String> features = new ArrayList<String>();

    features.add("word=" + currentWord);
    features.add("prevLabel=" + previousLabel);
    features.add("word=" + currentWord + ", prevLabel=" + previousLabel);
    return features;
  }

}
