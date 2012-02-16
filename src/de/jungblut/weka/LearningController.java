package de.jungblut.weka;

import au.com.bytecode.opencsv.CSVWriter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
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
        List<String[]> trainingSet = prepareInput(getTrainingInput());
        List<String[]> validationSet = prepareInput(getValidationInput());
        try (CSVWriter writer = prepareOutput()) {
            List<ClassificationTask> tasks = getTasks();
            for (ClassificationTask task : tasks) {
                final int taskId = task.getId();
                final List<String[]> filteredInput = filter(validationSet, taskId, getFilterColumn());
                Instances instances = new Instances(taskId + "_dataset", task.getAttributes(), filteredInput.size());
                instances.setClassIndex(task.getPredictionAttributeIndex());
                for (String[] line : filteredInput) {
                    instances.add(task.parseTrainingInstance(line));
                }
                saveInstances(taskId, false, instances);

                final List<Filter> filterList = task.getFilterList();
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

                final List<String[]> filteredValidationSet = filter(validationSet, task.getId(), getFilterColumn());
                final Attribute targetAttribute = task.getAttributes().get(task.getPredictionAttributeIndex());
                for (String[] s : filteredValidationSet) {
                    Instance inst = task.parseInstance(s);
                    inst.setDataset(instances);
                    for (Filter f : filterList) {
                        f.input(inst);
                        inst = f.output();
                    }
                    int index = (int) classifier.classifyInstance(inst);
                    String prediction = targetAttribute.value(index);
                    String[] output = task.prepareResult(s, prediction, isDebug());
                    // TODO buffer and maybe sort on a specific column
                    writer.writeNext(output);
                }
            }
        }
    }

    private void saveInstances(int taskId, boolean filtered,
                               Instances instances) throws IOException {
        save(instances, filtered ? new File(getOutput(), taskId + "_filtered.arff")
                : new File(getOutput(), taskId + ".arff"));
    }

    private void save(Instances instances, File f)
            throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        saver.setFile(f);
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

    private List<String[]> prepareInput(File f) {
        List<String[]> lineList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null)
                lineList.add(line.split("\t"));

            lineList.remove(0);
        } catch (IOException error) {
            error.printStackTrace();
        }
        return lineList;
    }

    public abstract File getTrainingInput();

    public abstract File getValidationInput();

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
