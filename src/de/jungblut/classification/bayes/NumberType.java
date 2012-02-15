package de.jungblut.classification.bayes;

import java.util.LinkedList;
import java.util.List;

public class NumberType implements Type {

    private String className;

    private List<Double> inputList = new LinkedList<>();

    private double mean;
    private double variance;

    @Override
    public void addInput(String input) {
        inputList.add(Double.valueOf(input));
    }

    @Override
    public double getProbability(String input, double aprioriProbability) {
        double in = Double.valueOf(input);
        // using normal density distribution
        return (1.0 / (Math.sqrt(2 * Math.PI * variance)))
                * Math.pow(Math.E, (-(Math.pow((in - mean), 2)))
                / (2 * variance));
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
        double sum = 0.0;
        for (Double d : inputList)
            sum += d;

        mean = sum / inputList.size();

        double squaredVarianceSum = 0.0;
        for (Double d : inputList)
            squaredVarianceSum += Math.pow((d - mean), 2);

        variance = squaredVarianceSum / inputList.size();

        inputList = null;
    }

    @Override
    public Type clone() {
        NumberType type = new NumberType();
        type.setAttributeName(getAttributeName());
        return type;
    }

}
