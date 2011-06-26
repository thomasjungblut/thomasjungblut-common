package de.jungblut.classification;

public class TypeHelper {

	static final Type getTypeForString(String type) {
		if (type.equals("n")) {
			return new NumberType();
		} else {
			return null;
		}
	}

}
