package de.jungblut.classification;

import java.util.LinkedList;
import java.util.List;

public class StringType implements Type {

	private String className;

	List<String> inputList = new LinkedList<String>();

	@Override
	public void addInput(String input) {
		inputList.add(input);
	}

	@Override
	public double getProbability(String input, double aprioriProbability) {
		return 1.0f;
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
		// TODO
	}

	@Override
	public Type clone() {
		StringType type = new StringType();
		type.setAttributeName(getAttributeName());
		return type;
	}

}
