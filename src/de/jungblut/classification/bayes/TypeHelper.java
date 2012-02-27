package de.jungblut.classification.bayes;

class TypeHelper {

  static Type getTypeForString(String type) {
    switch (type) {
      case "n":
        return new NumberType();
      case "s":
        return new StringType();
      case "b":
        return new BooleanType();
      default:
        return null;
    }
  }

}
