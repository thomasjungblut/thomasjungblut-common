package de.jungblut.classification.bayes;

import java.util.Arrays;

public class TrainingSet {

  /**
   * column 0 is always the classname of the training every followed column is
   * an attribute. <br/>
   * set [0] -> this line is the header. <br/>
   * set[0][0] contains all classes, seperated by a semicolon.<br/>
   * set[1][>0] contains the datatypes name
   * <p/>
   * 
   * <pre>
   * male;female 	height (cm) 	weight (kg) 	foot size(cm)
   *   				n				n				n
   *  male 			180 			90 				36
   * 	male 			190 			110 			33
   * </pre>
   */
  final String[][] set;

  private TrainingSet(int attributeCount, int size) {
    super();
    set = new String[size + 2][attributeCount + 1];
  }

  void setHeader(String[] classes, String[] attributes) {
    String classString = "";
    for (String c : classes) {
      classString += c + ";";
    }
    set[0][0] = classString;
    System.arraycopy(attributes, 0, set[0], 1, attributes.length);
  }

  void setDataTypes(String[] types) {
    System.arraycopy(types, 0, set[1], 1, types.length);
  }

  // the first column always needs to be the classname.
  void setTrainingInput(String[][] lines) {
    for (int i = 0; i < lines.length; i++) {
      System.arraycopy(lines[i], 0, set[i + 2], 0, lines[i].length);
    }
  }

  public static TrainingSet getWikipediaTrainingsSet() {
    TrainingSet set = new TrainingSet(3, 8);

    set.setHeader(new String[] { "male", "female" }, new String[] {
        "height (cm)", "weight (kg)", "foot size(cm)" });

    set.setDataTypes(new String[] { "n", "n", "n" });
    set.setTrainingInput(new String[][] { { "male", "180", "90", "12" },
        { "male", "190", "95", "11" }, { "male", "200", "80", "12" },
        { "male", "195", "85", "10" }, { "female", "165", "60", "6" },
        { "female", "150", "55", "8" }, { "female", "156", "56", "7" },
        { "female", "160", "62", "9" } });

    return set;
  }

  @Override
  public String toString() {
    return "TrainingSet [set=" + Arrays.toString(set) + "]";
  }
}
