package de.jungblut.classification;

public interface Type extends Cloneable{

	public void addInput(String input);

	public double getProbability(String input, double aprioriProbability);

	public void setAttributeName(String input);

	public String getAttributeName();
	
	public void finalizeType();
	
	Type clone();
}
