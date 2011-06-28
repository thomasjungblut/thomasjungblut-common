package de.jungblut.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class TrainingSet {

	/**
	 * column 0 is always the classname of the training every followed column is
	 * an attribute. <br/>
	 * set [0] -> this line is the header. <br/>
	 * set[0][0] contains all classes, seperated by a semicolon.<br/>
	 * set[1][>0] contains the datatypes name
	 * 
	 * <pre>
	 * male;female 	height (cm) 	weight (kg) 	foot size(cm)
	 *   				n				n				n
	 *  male 			180 			90 				36
	 * 	male 			190 			110 			33
	 * </pre>
	 */
	String[][] set;

	public TrainingSet(int attributeCount, int size) {
		super();
		set = new String[size + 2][attributeCount + 1];
	}

	public void setHeader(String[] classes, String[] attributes) {
		String classString = "";
		for (String c : classes) {
			classString += c + ";";
		}
		set[0][0] = classString;
		for (int i = 0; i < attributes.length; i++) {
			set[0][i + 1] = attributes[i];
		}
	}

	public void setDataTypes(String[] types) {
		for (int i = 0; i < types.length; i++) {
			set[1][i + 1] = types[i];
		}
	}

	// the first column always needs to be the classname.
	public void setTrainingInput(String[][] lines) {
		for (int i = 0; i < lines.length; i++) {
			for (int column = 0; column < lines[i].length; column++) {
				set[i + 2][column] = lines[i][column];
			}
		}
	}

	public static TrainingSet getWikipediaTrainingsSet() {
		TrainingSet set = new TrainingSet(3, 8);

		set.setHeader(new String[] { "male", "female" }, new String[] { "height (cm)",
				"weight (kg)", "foot size(cm)" });

		set.setDataTypes(new String[] { "n", "n", "n" });
		set.setTrainingInput(new String[][] { { "male", "180", "90", "12" },
				{ "male", "190", "95", "11" }, { "male", "200", "80", "12" },
				{ "male", "195", "85", "10" }, { "female", "165", "60", "6" },
				{ "female", "150", "55", "8" }, { "female", "156", "56", "7" },
				{ "female", "160", "62", "9" } });

		return set;
	}

	public static TrainingSet getTextExample() {
		TrainingSet set = new TrainingSet(1, 8);

		set.setHeader(new String[] { "ID", "NAME" }, new String[] { "input" });

		set.setDataTypes(new String[] { "s" });
		set.setTrainingInput(new String[][] { { "ID", "productId" }, { "ID", "pid" },
				{ "ID", "produkt-id" }, { "ID", "p_id" }, { "NAME", "produktname" },
				{ "NAME", "name" }, { "NAME", "product-name" }, { "NAME", "pname" } });

		return set;
	}

	public static TrainingSet readCSVTrainingsSet() {

		LinkedList<String[]> file = new LinkedList<String[]>();
		HashSet<String> types = new HashSet<String>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					"res/csv_import_token.csv")));

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] arr = line.split(";");
				file.add(arr);
				types.add(arr[0]);
			}
			// remove the header of the CSV
			file.remove(0);

			TrainingSet set = new TrainingSet(1, file.size());

			set.setHeader(types.toArray(new String[types.size()]), new String[] { "NAME" });
			set.setDataTypes(new String[] { "s" });

			set.setTrainingInput(file.toArray(new String[file.size()][]));
			return set;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String toString() {
		return "TrainingSet [set=" + Arrays.toString(set) + "]";
	}
}
