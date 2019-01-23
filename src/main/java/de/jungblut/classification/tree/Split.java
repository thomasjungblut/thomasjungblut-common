package de.jungblut.classification.tree;

/**
 * From Mahout, split class with better naming.
 *
 * @author thomas.jungblut
 */
public final class Split {

    private final int featureIndex;
    private final double informationGain;
    private final double numericalSplitValue;

    public Split(int featureIndex, double informationGain,
                 double numericalSplitValue) {
        this.featureIndex = featureIndex;
        this.informationGain = informationGain;
        this.numericalSplitValue = numericalSplitValue;
    }

    public Split(int featureIndex, double informationGain) {
        this(featureIndex, informationGain, Double.NaN);
    }

    public int getSplitAttributeIndex() {
        return featureIndex;
    }

    public double getInformationGain() {
        return informationGain;
    }

    public double getNumericalSplitValue() {
        return numericalSplitValue;
    }

    @Override
    public String toString() {
        return "Split [featureIndex=" + this.featureIndex + ", informationGain="
                + this.informationGain + ", numericalSplitValue="
                + this.numericalSplitValue + "]";
    }

}
