package de.jungblut.classification.bayes;

import de.jungblut.similarity.CosineSimilarity;
import de.jungblut.similarity.Similarity;
import de.jungblut.similarity.Tokenizer;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class StringType implements Type {

    private String className;

    private final Similarity similarity = new CosineSimilarity();
    private final List<Set<String>> inputList = new LinkedList<>();

    @Override
    public void addInput(String input) {
        inputList.add(Tokenizer.tokenize(input, 3));
    }

    @Override
    public double getProbability(String input, double aprioriProbability) {
        // measure the mean similarity of all terms in the input list to the
        // input string
        double bestMatch = 0.0;
        final Set<String> inputTokens = Tokenizer.tokenize(input, 3);
        for (Set<String> term : inputList) {
            final double distance = similarity.measureDistance(term,
                    inputTokens);
            if (distance > bestMatch)
                bestMatch = distance;
        }

        // maybe measure against the apriorityProbability
        return bestMatch;

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

    }

    @Override
    public Type clone() {
        StringType type = new StringType();
        type.setAttributeName(getAttributeName());
        return type;
    }

}
