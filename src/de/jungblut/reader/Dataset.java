package de.jungblut.reader;

import de.jungblut.math.DoubleVector;

/**
 * Simplistic dataset to carry information about them. Needs vector
 * instantiation through constructor, names can be set on demand via setters.
 * 
 * @author thomas.jungblut
 * 
 */
public class Dataset {

  protected final DoubleVector[] features;
  protected final DoubleVector[] outcomes;

  // some additional info
  protected String[] featureNames;
  protected String[] classNames;

  public Dataset(DoubleVector[] features, DoubleVector[] outcomes) {
    super();
    this.features = features;
    this.outcomes = outcomes;
  }

  public boolean hasFeatureNames() {
    return featureNames != null;
  }

  public boolean hasClassNames() {
    return classNames != null;
  }

  public void setClassNames(String[] classNames) {
    this.classNames = classNames;
  }

  public void setFeatureNames(String[] featureNames) {
    this.featureNames = featureNames;
  }

  public String[] getClassNames() {
    return this.classNames;
  }

  public String[] getFeatureNames() {
    return this.featureNames;
  }

  public DoubleVector[] getFeatures() {
    return this.features;
  }

  public DoubleVector[] getOutcomes() {
    return this.outcomes;
  }

}
