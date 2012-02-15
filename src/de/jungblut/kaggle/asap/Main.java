package de.jungblut.kaggle.asap;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.Lists;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.instance.RemoveMisclassified;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * @author thomas.jungblut
 */
public class Main {

    private static final int NUM_TREES = 100;
    private static final boolean debug = false;

    private static final HashMap<Integer, Double> scaleFactors = new HashMap<>();

    static {
        scaleFactors.put(2, 1000.0d);
        scaleFactors.put(3, -1000.0d);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        BufferedReader br;
        if (debug) {
            br = new BufferedReader(new FileReader(new File(
                    "files/training_set_rel3.csv")));
        } else {
            br = new BufferedReader(new FileReader(new File(
                    "files/valid_set.csv")));
        }
        List<String[]> set = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null)
            set.add(line.split("\t"));

        set.remove(0);
        br.close();

        CSVWriter writer;
        if (debug) {
            writer = new CSVWriter(new FileWriter(new File(
                    "files/prediction.csv")), '\t', '\0');
            writer.writeNext(new String[]{"prediction_id", "essay_id",
                    "essay_set", "essay_weight", "predicted_score",
                    "real_score", "essay"});
        } else {
            writer = new CSVWriter(new FileWriter(new File(
                    "files/prediction.csv")), ',', '\0');
            writer.writeNext(new String[]{"prediction_id", "essay_id",
                    "essay_set", "essay_weight", "predicted_score"});
        }

        // TODO refactor this piece of crap...
        /**
         * - a class for each "i". <br>
         * - a specific filterset for each "i" <br>
         * - a specific attributeset for each "i" <br>
         */
        try {
            for (int i = 1; i <= 8; i++) {
                if (i != 2) {
                    Vectorizer v = new Vectorizer(i);
                    v.mapFilesIntoRAM();
                    Instances instances = v.extractTrainingset();
                    saveInstances(i, false, instances);
                    // attribute selection with CfsSubset and bestfirst makes it
                    // faster
                    List<Filter> filterChain = getFilterChain(i);
                    for (Filter f : filterChain) {
                        f.setInputFormat(instances);
                        instances = Filter.useFilter(instances, f);
                    }

                    saveInstances(i, true, instances);
                    Classifier classifier = getClassifier();
                    // if (i == 1 || i == 6 || i == 7 || i == 8)
                    // classifier = getPerceptron();
                    classifier.buildClassifier(instances);
                    Evaluation eTest = new Evaluation(instances);
                    // eTest.evaluateModel(classifier, testset);
                    eTest.crossValidateModel(classifier, instances, 10,
                            new Random(System.currentTimeMillis()));
                    System.out.println("Kappa score: " + eTest.kappa()
                            + " for essayset number " + i);
                    final POSTaggerME posTagger = v.getPosTagger();
                    final SentenceDetectorME sentDetector = v
                            .getSentenceDetector();
                    final List<String[]> filtered = Vectorizer.filter(
                            new ArrayList<>(set), i + "");
                    for (String[] s : filtered) {
                        Instance inst = getInstance(s, v, posTagger,
                                sentDetector);
                        inst.setDataset(instances);
                        for (Filter f : filterChain) {
                            f.input(inst);
                            inst = f.output();
                        }
                        int index = (int) classifier.classifyInstance(inst);
                        String prediction = v.prediction.value(index);

                        if (debug) {
                            writer.writeNext(new String[]{s[3], s[0], s[1],
                                    "1", prediction, s[6], s[2]});
                        } else {
                            writer.writeNext(new String[]{s[3], s[0], s[1],
                                    "1", prediction});
                        }
                    }
                } else if (i == 2) {
                    Vectorizer v = new Vectorizer(i);
                    final POSTaggerME posTagger = v.getPosTagger();
                    final SentenceDetectorME sentDetector = v
                            .getSentenceDetector();

                    BufferedReader br1 = new BufferedReader(new FileReader(
                            new File("files/training_set_rel3.csv")));
                    List<String[]> set1 = new ArrayList<>();
                    while ((line = br1.readLine()) != null)
                        set1.add(line.split("\t"));

                    set1.remove(0);
                    br1.close();

                    final List<String[]> filtered = Vectorizer.filter(
                            new ArrayList<>(set1), i + "");
                    Instances domain1Score = new Instances("domain1Score",
                            v.attributes, 10);
                    domain1Score.setClassIndex(v.attributes.size() - 1);
                    // new vectorizer for -2
                    Vectorizer v2 = new Vectorizer(-2);
                    Instances domain2Score = new Instances("domain2Score",
                            v2.attributes, 10);
                    domain2Score.setClassIndex(v2.attributes.size() - 1);
                    for (String[] s : filtered) {
                        final Instance inst = getInstance(s, v, posTagger,
                                sentDetector);
                        v.mode = 0; // this takes from column 6
                        domain1Score.add(v.addScore(s, deepcopy(inst)));
                        v.mode = 1; // this takes from column 9
                        domain2Score.add(v.addScore(s, deepcopy(inst)));
                    }

                    save(domain2Score, "files/weka_out/" + i + "_1.arff");
                    save(domain2Score, "files/weka_out/" + i + "_2.arff");

                    List<Filter> domain1Filter = getFilterChain(i);
                    for (Filter f : domain1Filter) {
                        f.setInputFormat(domain1Score);
                        domain1Score = Filter.useFilter(domain1Score, f);
                    }
                    List<Filter> domain2Filter = getFilterChain(i);
                    for (Filter f : domain2Filter) {
                        f.setInputFormat(domain2Score);
                        domain2Score = Filter.useFilter(domain2Score, f);
                    }

                    save(domain2Score, "files/weka_out/" + i
                            + "_1_filtered.arff");
                    save(domain2Score, "files/weka_out/" + i
                            + "_2_filtered.arff");

                    Classifier classifier1 = getClassifier();
                    classifier1.buildClassifier(domain1Score);

                    Evaluation eTest = new Evaluation(domain1Score);
                    // eTest.evaluateModel(classifier, testset);
                    eTest.crossValidateModel(classifier1, domain1Score, 10,
                            new Random(System.currentTimeMillis()));
                    System.out.println("Kappa score for instance 1: "
                            + eTest.kappa() + " for essayset number " + i);

                    Classifier classifier2 = getClassifier();
                    classifier2.buildClassifier(domain2Score);

                    eTest = new Evaluation(domain2Score);
                    // eTest.evaluateModel(classifier, testset);
                    eTest.crossValidateModel(classifier2, domain2Score, 10,
                            new Random(System.currentTimeMillis()));
                    System.out.println("Kappa score for instance 2: "
                            + eTest.kappa() + " for essayset number " + i);

                    final List<String[]> filteredValidation = Vectorizer
                            .filter(new ArrayList<>(set), i + "");
                    for (String[] s : filteredValidation) {
                        Instance globalInstance = getInstance(s, v, posTagger,
                                sentDetector);
                        Instance inst = deepcopy(globalInstance);
                        inst.setDataset(domain1Score);
                        for (Filter f : domain1Filter) {
                            f.input(inst);
                            inst = f.output();
                        }
                        int index = (int) classifier1.classifyInstance(inst);
                        String prediction1 = v.prediction.value(index);

                        if (debug) {
                            writer.writeNext(new String[]{s[3], s[0], s[1],
                                    "0.5", prediction1, s[9], s[2]});
                        } else {
                            writer.writeNext(new String[]{s[3], s[0], s[1],
                                    "0.5", prediction1});
                        }

                        inst = deepcopy(globalInstance);
                        inst.setDataset(domain2Score);
                        for (Filter f : domain2Filter) {
                            f.input(inst);
                            inst = f.output();
                        }
                        index = (int) classifier2.classifyInstance(inst);
                        String prediction2 = v.prediction.value(index);

                        if (debug) {
                            writer.writeNext(new String[]{s[4], s[0], s[1],
                                    "0.5", prediction2, s[9], s[2]});
                        } else {
                            writer.writeNext(new String[]{s[4], s[0], s[1],
                                    "0.5", prediction2});
                        }
                    }
                }
            }
        } finally {
            writer.close();
        }
        System.out.println("Finished!");
        Vectorizer.threadPool.shutdownNow();
    }

    private static void saveInstances(int i, boolean filtered,
                                      Instances instances) throws IOException {
        save(instances, filtered ? "files/weka_out/" + i + "_filtered.arff"
                : "files/weka_out/" + i + ".arff");
    }

    private static void save(Instances instances, String text)
            throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        saver.setFile(new File(text));
        saver.writeBatch();
    }

    // numerictonominal, standardize, discretize, sample and remove
    // missclassifications
    private static List<Filter> getFilterChain(int set) {
        NumericToNominal nom = new NumericToNominal();
        nom.setAttributeIndices("last");
        UnsupervisedStandardize stand = new UnsupervisedStandardize();
        Normalize normalizer = new Normalize();
        Double scale = scaleFactors.get(set);
        normalizer.setScale(scale != null ? scale : 1.0);
        SupervisedDiscretize disc = new SupervisedDiscretize();
        disc.setAttributeIndices("first-last");
        weka.filters.supervised.instance.Resample sample = new weka.filters.supervised.instance.Resample();
        RemoveMisclassified removeMisclassified = new RemoveMisclassified();
        removeMisclassified.setClassifier(getClassifier());
        AttributeSelection selector = new AttributeSelection();
        selector.setEvaluator(new GainRatioAttributeEval());
        selector.setSearch(new Ranker());
        return Lists.asList(nom, new Filter[]{stand, normalizer, disc,
                sample, selector, removeMisclassified});
    }

    private static Classifier getClassifier() {
        RandomForest classifier = new RandomForest();
        classifier.setNumTrees(NUM_TREES);
        classifier.setNumExecutionSlots(8);
        // read our more sophisticated model from file..
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream("files/customVote")));
            Object c = is.readObject();
            return (Classifier) c;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return classifier;
    }

    private static Instance getInstance(String[] element, Vectorizer vectorizer,
                                        POSTaggerME posTagger, SentenceDetectorME sentenceDetector) {
        return vectorizer.parseInstanceInternal(element, posTagger,
                sentenceDetector);
    }

    private static Instance deepcopy(Instance inst) {
        double[] toCopy = inst.toDoubleArray();
        double[] in = new double[toCopy.length];
        System.arraycopy(toCopy, 0, in, 0, toCopy.length);
        Instance newInstance = new DenseInstance(inst.weight(), in);
        newInstance.setDataset(inst.dataset());
        return newInstance;
    }

}
