package de.jungblut.kaggle.asap;

import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author thomas.jungblut
 */
final class Worker implements Callable<Instances> {

    private final List<String[]> sublist;
    private final Instances allocatedInstance;
    private final POSTaggerME tagger;
    private final SentenceDetectorME sentenceDetector;
    private final Vectorizer vectorizer;

    public Worker(Vectorizer vectorizer, List<String[]> sublist, Instances allocatedInstance, POSTaggerME tagger, SentenceDetectorME sentenceDetector) {
        this.vectorizer = vectorizer;
        this.sublist = sublist;
        this.allocatedInstance = allocatedInstance;
        this.tagger = tagger;
        this.sentenceDetector = sentenceDetector;
    }

    @Override
    public final Instances call() throws Exception {
        for (String[] line : sublist) {
            final Instance instance = vectorizer.parseInstance(line, tagger, sentenceDetector);
            allocatedInstance.add(instance);
        }
        return allocatedInstance;
    }
}
