package de.jungblut.datastructure;

/**
 * Immutable generic distance result that contains a document type object and
 * its distance (to some artificial queried document).
 *
 * @param <TYPE> the type of the document.
 * @author thomas.jungblut
 */
public class DistanceResult<TYPE> {

    private final double distance;
    private final TYPE document;

    /**
     * Create a new {@link DistanceResultImpl} with a distance and a document.
     *
     * @param distance the distance.
     * @param document the document.
     */
    public DistanceResult(double distance, TYPE document) {
        this.distance = distance;
        this.document = document;
    }

    /**
     * @return the distance.
     */
    public double getDistance() {
        return this.distance;
    }

    /**
     * @return the document.
     */
    public TYPE get() {
        return this.document;
    }

    @Override
    public String toString() {
        return document + " | " + distance;
    }

}
