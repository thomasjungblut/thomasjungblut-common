package de.jungblut.weka;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.filters.Filter;

import java.util.ArrayList;
import java.util.List;

public interface ClassificationTask {

    public ArrayList<Attribute> getAttributes();

    public int getPredictionAttributeIndex();

    public List<Filter> getFilterList();

    public Classifier getClassifier();

    public Instance parseTrainingInstance(String[] data);

    public Instance parseInstance(String[] data);

    public int getId();

    public String[] prepareResult(String[] input, String prediction, boolean debug);
}
