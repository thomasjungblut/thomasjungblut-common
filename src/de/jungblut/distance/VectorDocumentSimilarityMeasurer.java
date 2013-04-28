package de.jungblut.distance;

import java.util.Set;

import de.jungblut.datastructure.InvertedIndex.DocumentSimilarityMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * Document similarity measurer on vectors (basically a proxy to the real
 * {@link DistanceMeasurer} by the {@link SimilarityMeasurer}).
 * 
 * @author thomas.jungblut
 * 
 * @param <T> the possible key type. On sparse vectors where inverted indices
 *          are used, this is the dimension where the value not equals 0.
 */
public final class VectorDocumentSimilarityMeasurer<T> implements
    DocumentSimilarityMeasurer<DoubleVector, T> {

  private final SimilarityMeasurer measurer;

  private VectorDocumentSimilarityMeasurer(SimilarityMeasurer measurer) {
    this.measurer = measurer;
  }

  @Override
  public double measure(DoubleVector reference, Set<T> referenceKeys,
      DoubleVector doc, Set<T> docKeys) {
    return measurer.measureSimilarity(reference, doc);
  }

  /**
   * @return a new vector document similarity measurer by a similarity measure.
   */
  public static <T> VectorDocumentSimilarityMeasurer<T> with(
      SimilarityMeasurer measurer) {
    return new VectorDocumentSimilarityMeasurer<>(measurer);
  }

  /**
   * @return a new vector document similarity measurer by a distance measure.
   */
  public static <T> VectorDocumentSimilarityMeasurer<T> with(
      DistanceMeasurer measurer) {
    return new VectorDocumentSimilarityMeasurer<>(new SimilarityMeasurer(
        measurer));
  }

}
