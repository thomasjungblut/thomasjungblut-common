package de.jungblut.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class NaiveBayesClassifier {

	private final TrainingSet trainingSet;

	private final Map<String, ArrayList<Type>> classTypeMap = new HashMap<String, ArrayList<Type>>();

	// we assume that we have an equal probability for each class.
	private double equalProbability;

	// or just use a hashmap that tracks the frequency in the trainingsset
	// HashMap<String, Double> probabilityMap = new HashMap<String, Double>();

	public NaiveBayesClassifier(TrainingSet trainingSet) {
		super();
		this.trainingSet = trainingSet;
		setupTypes();
		train();
	}

	public PriorityQueue<ProbabilityResult> measureProbability(
			String[] inputAttributes) {
		PriorityQueue<ProbabilityResult> posterior = new PriorityQueue<ProbabilityResult>();
		for (Entry<String, ArrayList<Type>> entry : classTypeMap.entrySet()) {
			double p = equalProbability;
			for (int i = 0; i < inputAttributes.length; i++) {
				double probability = entry.getValue().get(i)
						.getProbability(inputAttributes[i], equalProbability);
				p = p * probability;
			}
			posterior.add(new ProbabilityResult(entry.getKey(), p));
		}
		return posterior;
	}

	private static class ProbabilityResult implements
			Comparable<ProbabilityResult> {
		final String className;
		final double probability;

		public ProbabilityResult(String className, double probability) {
			super();
			this.className = className;
			this.probability = probability;
		}

		@Override
		public int compareTo(ProbabilityResult o) {
			return Double.compare(o.probability, probability);
		}

		@Override
		public String toString() {
			return "ProbabilityResult [className=" + className
					+ ", probability=" + probability + "]";
		}

	}

	private void train() {
		for (int i = 2; i < trainingSet.set.length; i++) {
			String[] line = trainingSet.set[i];
			ArrayList<Type> lineTypes = classTypeMap.get(line[0]);
			for (int column = 1; column < line.length; column++) {
				lineTypes.get(column - 1).addInput(line[column]);
			}
		}

		for (Entry<String, ArrayList<Type>> entry : classTypeMap.entrySet()) {
			for (Type t : entry.getValue())
				t.finalizeType();
		}
	}

	private void setupTypes() {
		for (String className : trainingSet.set[0][0].split(";")) {
			classTypeMap.put(className, null);
		}

		ArrayList<Type> typeList = new ArrayList<Type>();
		for (int i = 1; i < trainingSet.set[0].length; i++) {
			String attributeName = trainingSet.set[0][i];
			Type attributeType = TypeHelper
					.getTypeForString(trainingSet.set[1][i]);
			attributeType.setAttributeName(attributeName);
			typeList.add(attributeType);
		}
		for (String key : classTypeMap.keySet()) {
			classTypeMap.put(key, deepCopy(typeList));
		}
		equalProbability = 1.0 / classTypeMap.size();
	}

	private ArrayList<Type> deepCopy(ArrayList<Type> list) {
		ArrayList<Type> copy = new ArrayList<Type>(list.size());
		for (Type t : list)
			copy.add(t.clone());
		return copy;
	}

	public static void main(String[] args) {
//		NaiveBayesClassifier classifier = new NaiveBayesClassifier(
//				TrainingSet.getWikipediaTrainingsSet());
//		System.out.println(classifier.measureProbability(new String[] { "150",
//				"50", "6" }));
//
//		NaiveBayesClassifier c = new NaiveBayesClassifier(
//				TrainingSet.getTextExample());
//		System.out.println(c.measureProbability(new String[] { "prdkname" }));

		NaiveBayesClassifier c = new NaiveBayesClassifier(
				TrainingSet.readCSVTrainingsSet());
		System.out.println(c.measureProbability(new String[] { "id" }));
		
	}

}
