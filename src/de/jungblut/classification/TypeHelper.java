package de.jungblut.classification;

public class TypeHelper {

	static final Type getTypeForString(String type) {
		if (type.equals("n")) {
			return new NumberType();
		} else if (type.equals("s")) {
			return new StringType();
		} else {
			return null;
		}
	}

}
