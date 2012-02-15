package de.jungblut.weka;

import au.com.bytecode.opencsv.CSVWriter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class LearningController {

    private boolean debug;

    public void compute() throws Exception {
        List<String[]> input = prepareInput();
        CSVWriter writer = prepareOutput();
        List<ClassificationTask> tasks = getTasks();

        for (ClassificationTask task : tasks) {
            final int taskId = task.getId();
            final List<String[]> filteredInput = filter(input, taskId, getFilterColumn());
            Instances instances = new Instances(taskId + "_dataset", task.getAttributes(), filteredInput.size());
            instances.setClassIndex(task.getPredictionAttributeIndex());
            for (String[] line : filteredInput) {
                instances.add(task.parseTrainingInstance(line));
            }
            saveInstances(taskId, false, instances);

            List<Filter> filterList = task.getFilterList();
            for (Filter f : filterList) {
                instances = Filter.useFilter(instances, f);
            }
            saveInstances(taskId, true, instances);

            Classifier classifier = task.getClassifier();
            classifier.buildClassifier(instances);

            Evaluation eTest = new Evaluation(instances);
            eTest.crossValidateModel(classifier, instances, 10,
                    new Random(System.currentTimeMillis()));
            outputEvaluation(eTest);


        }
    }

    private void saveInstances(int taskId, boolean filtered,
                               Instances instances) throws IOException {
        save(instances, filtered ? "files/weka_out/" + taskId + "_filtered.arff"
                : "files/weka_out/" + taskId + ".arff");
    }

    private void save(Instances instances, String text)
            throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        saver.setFile(new File(text));
        saver.writeBatch();
    }

    private List<String[]> filter(List<String[]> set, int setId,
                                        int filterColumn) {
        if (setId > 0) {
            List<String[]> list = new ArrayList<>();
            final Iterator<String[]> iterator = set.iterator();
            String s = setId + "";
            while (iterator.hasNext()) {
                final String[] next = iterator.next();
                if (next[filterColumn].equals(s)) {
                    list.add(next);
                }
            }
            return list;
        }
        return set;
    }

    private CSVWriter prepareOutput() throws IOException {
        CSVWriter writer;
        if (debug) {
            writer = new CSVWriter(new FileWriter(getOutput()), '\t', '\0');
        } else {
            writer = new CSVWriter(new FileWriter(getOutput()), ',', '\0');
        }
        writer.writeNext(getHeader());
        return writer;
    }

    private List<String[]> prepareInput() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(getInput()));
        List<String[]> lineList = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null)
            lineList.add(line.split("\t"));

        lineList.remove(0);
        br.close();
        return lineList;
    }

    public abstract File getInput();

    public abstract File getOutput();

    public abstract List<ClassificationTask> getTasks();

    public abstract String[] getHeader();

    public abstract int getFilterColumn();

    public abstract void outputEvaluation(Evaluation e);

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
