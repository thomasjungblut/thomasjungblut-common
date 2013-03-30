package de.jungblut.ner;

import java.util.List;

/**
 * Interface for feature extraction in sequence learning. Can be used with the
 * {@link SparseFeatureExtractorHelper} to calculate real vectors to use in a
 * classifier.
 * 
 * @author thomas.jungblut
 * 
 */
public interface SequenceFeatureExtractor<K> {

  /**
   * Compute a feature for the given sequence (the complete list words). Given
   * are the previous label and the current index (position). This method will
   * be called for every index in the list of words.
   * 
   * @param words all words in that sequence.
   * @param prevLabel the previous label.
   * @param position the current position.
   * @return a set of features for this position.
   */
  public List<String> computeFeatures(List<K> words, int prevLabel,
      int position);

}
