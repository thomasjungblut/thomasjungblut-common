package de.jungblut.clustering;

import de.jungblut.math.DoubleVector;

import java.util.List;

public class Cluster {

    private final DoubleVector center;
    private final List<DoubleVector> assignments;

    public Cluster(DoubleVector center, List<DoubleVector> assignments) {
        this.center = center;
        this.assignments = assignments;
    }

    public DoubleVector getCenter() {
        return this.center;
    }

    public List<DoubleVector> getAssignments() {
        return this.assignments;
    }

}
