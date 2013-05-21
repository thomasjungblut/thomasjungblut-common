package de.jungblut.distance;

import java.util.Set;

import de.jungblut.datastructure.InvertedIndex.DocumentDistanceMeasurer;
import de.jungblut.math.DoubleVector;

/**
 * Document distance measurer on vectors (basically a proxy to the real
 * {@link DistanceMeasurer}).
 * 
 * @author thomas.jungblut
 * 
 * @param <T> the possible key type. On sparse vectors where inverted indices
 *          are used, this is the dimension where the value not equals 0.
 */
public final class VectorDocumentDistanceMeasurer<T> implements
    DocumentDistanceMeasurer<DoubleVector, T> {

  private final DistanceMeasurer measurer;

  private VectorDocumentDistanceMeasurer(DistanceMeasurer measurer) {
    this.measurer = measurer;
  }

  @Override
  public double measure(DoubleVector reference, Set<T> referenceKeys,
      DoubleVector doc, Set<T> docKeys) {
    return measurer.measureDistance(reference, doc);
  }

  /**
   * @return a new vector document similarity measurer by a distance measure.
   */
  public static <T> VectorDocumentDistanceMeasurer<T> with(
      DistanceMeasurer measurer) {
    return new VectorDocumentDistanceMeasurer<>(measurer);
  }

}
