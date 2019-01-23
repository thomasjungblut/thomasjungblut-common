package de.jungblut.nlp;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleVector;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simply distance measure wrapper for debug string similarity measuring.
 *
 * @author thomas.jungblut
 */
public final class DocumentSimilarity {

    private final DistanceMeasurer measurer;

    private DocumentSimilarity(DistanceMeasurer measurer) {
        super();
        this.measurer = measurer;
    }

    /**
     * @return a double where 1 is similar and 0 is not.
     */
    public final double measureDocumentSimilarity(String[] doc1, String[] doc2) {
        List<DoubleVector> wordFrequencyVectorize = VectorizerUtils
                .wordFrequencyVectorize(doc1, doc2).collect(Collectors.toList());
        // invert again, because we want the similarity
        return 1.0d - measurer.measureDistance(wordFrequencyVectorize.get(0),
                wordFrequencyVectorize.get(1));
    }

    public static DocumentSimilarity with(DistanceMeasurer measurer) {
        return new DocumentSimilarity(measurer);
    }

}
