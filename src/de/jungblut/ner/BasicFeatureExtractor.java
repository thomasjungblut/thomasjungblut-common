package de.jungblut.ner;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic feature extraction for sequence learning, takes the current word into
 * account and the previous label - as well as the joint version of both. This
 * will implicitly create a dictionary in your features. <br/>
 * This was used to derive names from an english text.
 * 
 * @author thomas.jungblut
 * 
 */
public final class BasicFeatureExtractor implements
    SequenceFeatureExtractor<String> {

  @Override
  public List<String> computeFeatures(List<String> words, int previousLabel,
      int position) {
    String currentWord = words.get(position);
    List<String> features = new ArrayList<>();

    features.add("word=" + currentWord);
    features.add("prevLabel=" + previousLabel);
    features.add("word=" + currentWord + ", prevLabel=" + previousLabel);
    features.add("upperCharBegin="
        + Character.isUpperCase(currentWord.charAt(0)));
    features.add("length=" + currentWord.length());
    features.add("alphabetic="
        + (currentWord.replaceAll("[^A-Za-z]", "").length() > 0));
    return features;
  }

}
