package de.jungblut.classification.tree;

import java.util.Arrays;

/**
 * Denotes a feature type, either numerical or nominal. This is majorly used in
 * {@link DecisionTree}.
 * 
 * @author thomasjungblut
 * 
 */
public enum FeatureType {

  /*
   * Never ever change the order, just append!
   */
  NOMINAL, NUMERICAL;

  /**
   * @return true if this is numerical.
   */
  public boolean isNumerical() {
    return this == NUMERICAL;
  }

  /**
   * @return true if this is nominal.
   */
  public boolean isNominal() {
    return this == NOMINAL;
  }

  /**
   * Creates an array that denotes the type of a feature at a given feature
   * index. This method sets all features to a nominal/categorical type.
   * 
   * @param numFeatures the number of features.
   * @return the array that contains the type mappings.
   */
  public static FeatureType[] allNominal(int numFeatures) {
    FeatureType[] types = new FeatureType[numFeatures];
    Arrays.fill(types, FeatureType.NOMINAL);
    return types;
  }

  /**
   * Creates an array that denotes the type of a feature at a given feature
   * index. This method sets all features to a numeric type.
   * 
   * @param numFeatures the number of features.
   * @return the array that contains the type mappings.
   */
  public static FeatureType[] allNumerical(int numFeatures) {
    FeatureType[] types = new FeatureType[numFeatures];
    Arrays.fill(types, FeatureType.NUMERICAL);
    return types;
  }

}
