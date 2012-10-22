package de.jungblut.classification;

/**
 * Factory interface for building new classifiers, majorly used in
 * crossvalidation to generate new classifiers when needed.
 * 
 * @author thomas.jungblut
 * 
 */
public interface ClassifierFactory {

  /**
   * @return a new instance of a classifier.
   */
  public Classifier newInstance();

}
