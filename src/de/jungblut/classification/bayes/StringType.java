package de.jungblut.classification.bayes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jungblut.distance.CosineDistance;
import de.jungblut.nlp.DocumentSimilarity;
import de.jungblut.nlp.Tokenizer;

public class StringType implements Type {

  private String className;

  private final DocumentSimilarity similarity = DocumentSimilarity
      .with(new CosineDistance());
  private final List<Set<String>> inputList = new LinkedList<>();

  @Override
  public void addInput(String input) {
    inputList.add(new HashSet<String>(Arrays.asList(Tokenizer.nGrammTokenize(
        input, 3))));
  }

  @Override
  public double getProbability(String input, double aprioriProbability) {
    // measure the mean similarity of all terms in the input list to the
    // input string
    double bestMatch = 0.0;
    final Set<String> inputTokens = new HashSet<String>(Arrays.asList(Tokenizer
        .nGrammTokenize(input, 3)));
    for (Set<String> term : inputList) {
      final double distance = similarity.measureDocumentSimilarity(
          term.toArray(new String[term.size()]),
          inputTokens.toArray(new String[inputTokens.size()]));
      if (distance < bestMatch)
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
