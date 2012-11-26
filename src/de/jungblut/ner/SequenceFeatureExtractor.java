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
public interface SequenceFeatureExtractor {

  /**
   * Compute a feature for the given
   * 
   * @param words all words in that sequence.
   * @param prevLabel the previous label.
   * @param position the current position.
   * @return a set of features for this position.
   */
  public List<String> computeFeatures(List<String> words, int prevLabel,
      int position);

}
