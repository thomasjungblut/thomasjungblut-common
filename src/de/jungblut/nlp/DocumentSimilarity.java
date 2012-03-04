package de.jungblut.nlp;

import java.util.List;

import de.jungblut.similarity.DistanceMeasurer;

/**
 * Simply distance measure wrapper for debug string similarity measuring.
 * 
 * @author thomas.jungblut
 */
public class DocumentSimilarity {

  private final DistanceMeasurer measurer;

  private DocumentSimilarity(DistanceMeasurer measurer) {
    super();
    this.measurer = measurer;
  }

  /**
   * @return a double where 1 is similar and 0 is not.
   */
  public double measureDocumentSimilarity(String[] doc1, String[] doc2) {
    List<double[]> wordFrequencyVectorize = Vectorizer.wordFrequencyVectorize(
        doc1, doc2);
    // invert again, because we want the similarity
    return 1.0d - measurer.measureDistance(wordFrequencyVectorize.get(0),
        wordFrequencyVectorize.get(1));
  }

  public static DocumentSimilarity with(DistanceMeasurer measurer) {
    return new DocumentSimilarity(measurer);
  }

}
