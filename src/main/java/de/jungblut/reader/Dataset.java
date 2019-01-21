package de.jungblut.reader;

import java.util.Arrays;
import java.util.stream.Stream;

import com.codepoetics.protonpack.StreamUtils;

import de.jungblut.math.DoubleVector;
import de.jungblut.online.ml.FeatureOutcomePair;

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

  public Stream<FeatureOutcomePair> asStream() {
    Stream<DoubleVector> left = Arrays.stream(getFeatures());
    Stream<DoubleVector> right = Arrays.stream(getOutcomes());
    return StreamUtils.zip(left, right, (l, r) -> new FeatureOutcomePair(l, r));
  }

}
