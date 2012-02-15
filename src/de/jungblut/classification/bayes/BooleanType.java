package de.jungblut.classification.bayes;

public class BooleanType implements Type {

    private String className;
    /*
      * booleanCount[0] is the count for false. At index 1 is the count for true.
      */
    private final int[] booleanCount = new int[2];

    private double probabilityForTrue;

    @Override
    public void addInput(String input) {
        if (Boolean.valueOf(input)) {
            booleanCount[1] = booleanCount[1] + 1;
        } else {
            booleanCount[0] = booleanCount[0] + 1;
        }
    }

    @Override
    public double getProbability(String input, double aprioriProbability) {
        boolean b = Boolean.valueOf(input);
        if (b) {
            return probabilityForTrue * aprioriProbability;
        } else {
            return (1 - probabilityForTrue) * aprioriProbability;
        }
    }

    @Override
    public void setAttributeName(String input) {
        className = input;
    }

    @Override
    public String getAttributeName() {
        return className;
    }

    @Override
    public void finalizeType() {
        int size = booleanCount[0] + booleanCount[1];
        probabilityForTrue = (double) booleanCount[1] / (double) size;
    }

    @Override
    public Type clone() {
        BooleanType type = new BooleanType();
        type.setAttributeName(getAttributeName());
        return type;
    }

}
